package com.b502lab.ctsp.ito;

import com.b502lab.ctsp.aco.ConstructSolution;
import com.b502lab.tsp.aco.AS;

import java.util.Arrays;
import java.util.List;

import static com.b502lab.ctsp.common.Ctsp.me;
import static com.b502lab.tsp.common.Constant.*;

/**
 * 原来求解vrp问题的ito求解ctsp
 *
 * @author yonglecai
 */
public class ITO extends AS {

    private int noUpdateTime;// 算法的连续未更新迭代次数
    private int noUpdateTime2;
    private int curGen = 1;// 当前进化代数

    private double T;// 环境温度
    private double[] alfa;// 微粒的运动能力
    private double[] radius;// 粒子的半径

    public ITO(int n, int[][] distance) {
        super(n, distance);

        T = T0;// 初始化环境温度
        alfa = new double[nAnts];
        radius = new double[nAnts];
    }

    @Override
    protected void resetTau() {

        m_tau = new double[me.n][me.n];
        for (int i = 0; i < me.n; i++) {
            for (int j = 0; j < i; j++) {
                if (me.canVisit(i, j)) {
                    setTau(i, j, INITW);
                }
            }
        }
    }

    /**
     * 更新权重信息
     *
     * @param index 要更新权重的粒子索引
     */
    private void updateWeight(int index) {

        double sum = 0;
        double avgW;
        /*-------------计算信息素浓度平均值--------------*/
        for (int i = 0; i < me.n; i++) {
            for (int j = 0; j < i; j++) {
                sum += m_tau[i][j];
            }
        }
        // TODO 除数是否需要优化？？？
        avgW = (sum + 0.0) / (me.n * (me.n - 1) * 0.5);
        // System.out.println(avgW + "\t" + m_nAnts[index].alfa + "\t" + T
        // + "\t" + allBestParticle.fitness + "\t"
        // + allBestParticle.disLen);

		/*-------------将路径分级--------------*/
        int[][] g = new int[me.n][me.n];

        for (int i = 0, j, k; i < m_nAnts[index].size() - 1; i++) { // 当前解
            j = m_nAnts[index].get(i);
            k = m_nAnts[index].get(i + 1);
            if (j == k)
                continue;
            g[j][k] += 1;
            g[k][j] += 1;
        }
        for (int i = 0, j, k; i < iter_best.size() - 1; i++) { // 当前最优解
            j = iter_best.get(i);
            k = iter_best.get(i + 1);
            if (j == k)
                continue;
            g[j][k] += 2;
            g[k][j] += 2;
        }
        /*-------------更新信息素浓度--------------*/
        for (int i = 0; i < me.n; i++) {
            for (int j = 0; j < me.n; j++) {
                if (!me.canVisit(i, j) || i == j)
                    continue;
                if (g[i][j] >= 2) {
                    setTau(i, j, m_tau[i][j] + alfa[index]);
                    if (m_tau[i][j] > 3 * avgW) {
                        m_tau[i][j] = 3 * avgW;
                    }
                } else if (g[i][j] == 0) {
                    if (p > Math.random()) {
                        setTau(i, j, m_tau[i][j] + alfa[index]);
                        if (m_tau[i][j] > 3 * avgW) {
                            m_tau[i][j] = 3 * avgW;
                        }
                    }
                }
            }
        }
    }

    protected void globalUpdatingRule() {
        /*-------------信息素浓度以0.9的倍率挥发--------------*/
        for (int i = 0; i < me.n; i++) {
            for (int j = 0; j < i; j++) {
                if (!me.canVisit(i, j)) {
                    setTau(i, j, 0);
                    continue;
                }
                setTau(i, j, (1 - A) * m_tau[i][j]);
                if (m_tau[i][j] < 0.01) {
                    setTau(i, j, 0.01);
                }
            }
        }
        /*---------所有微粒更新信息素浓度，然后构造新的路径---------*/
        for (int i = 0; i < m_nAnts.length; i++) {
            updateWeight(m_nAnts.length - 1 - i);
            updatePath(i);
        }
    }

    private void updatePath(int i) {
//        List<Integer> curTour = ConstructSolution.constructSolution(this);
        List<Integer> curTour = UpdateSolution.updatePath(m_tau);
        int curFit = computePathValue(curTour);
        m_nAnts[m_nAnts.length - 1 - i] = curTour;
        values[m_nAnts.length - 1 - i] = curFit;
    }

    private void updateTemperature() {
        if (curGen % TLength == 0) {
            T *= ρ;
            if (T < 1) {
                T = 1;
            }
        }
    }

    private void updateAllRadius() {
        for (int i = 0; i < m_nAnts.length; i++) {
            radius[i] = ((1.0 / M) * i + PRECISION);
        }
    }

    private void updateAllAlfa() {
        for (int i = 0; i < m_nAnts.length; i++) {
            double fr = 0, ft = 0;
            double eλ = Math.exp(-λ);

            fr = (Math.exp(-λ * radius[i]) - eλ + 0.0) / (1.0 - eλ);
            ft = Math.exp(-1.0 / T);
            alfa[i] = fr * ft + PRECISION;
        }
    }

    private void updateBestAndNoUpdateTime() {
        /*-------已经对粒子群按照适应度值进行降序排序，所有最后一个为最优解-------*/
        iter_best = cloneList(m_nAnts[m_nAnts.length - 1]);
        iter_best_value = values[values.length - 1];

		/*-------更新全局最优解-------*/
        if (so_far_best_value > iter_best_value) {
            so_far_best = cloneList(iter_best);
            so_far_best_value = iter_best_value;
            noUpdateTime = 0;
            noUpdateTime2 = 0;
        } else {
            noUpdateTime++;
            noUpdateTime2++;
            if (noUpdateTime >= 20) {
                resetTau();
                noUpdateTime = 0;
            } else if (noUpdateTime2 >= 60) {
                curGen = GEN;
            }
        }
    }

    private void sort() {
        for (int i = 0; i < values.length - 1; i++) {
            int k = i;
            for (int j = i + 1; j < values.length; j++) {
                if (values[j] > values[k])
                    k = j;
            }
            if (i != k) {
                int t = values[i];
                values[i] = values[k];
                values[k] = t;

                List tt = m_nAnts[i];
                m_nAnts[i] = m_nAnts[k];
                m_nAnts[k] = tt;
            }
        }
    }

    @Override
    public void execute() {
        // LinkedList<Double> bestlist = new LinkedList<>();
        int count = 0;
        for (int i = 0; i < m_nAnts.length; i++) {
            updatePath(i);
        }
        for (curGen = 1; curGen <= GEN; curGen++) {
            count++;
            /*-----对粒子群进行排序，按距离从大到小-------*/
            sort();
            /*------找到本轮迭代的最优解和全局最优解------*/
            updateBestAndNoUpdateTime();

			/*-------更新各微粒半径-------*/
            updateAllRadius();// 基于排序的方法
            /*-----更新环境温度-------*/
            updateTemperature();
            /*------更新运动能力------*/
            updateAllAlfa();
            // bestlist.add(allBestParticle.fitness);
            /*-------更新所有粒子的路径-------*/
            globalUpdatingRule();
            // climping(); // 采用爬山法作为局部波动,更新种群
        }
    }

    @Override
    public double getBestValue() {
        return so_far_best_value;
    }

    @Override
    public List<Integer> getBestTour() {
        return so_far_best;
    }

}
