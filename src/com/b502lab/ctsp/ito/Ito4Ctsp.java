package com.b502lab.ctsp.ito;

import com.b502lab.ctsp.common.Base;

import java.util.Arrays;
import java.util.List;

import static com.b502lab.ctsp.common.Ctsp.me;
import static com.b502lab.tsp.common.Constant.*;

/**
 * 原来求解vrp问题的ito求解ctsp
 *
 * @author yonglecai
 */
public class Ito4Ctsp implements Base {

    private double T;// 环境温度

    private int noUpdateTime;// 算法的连续未更新迭代次数
    private int noUpdateTime2;
    private double[][] weight;// 信息素浓度
    private int curGen = 1;// 当前进化代数

    private Ito4CtspParticle particles[];// 微粒群
    private Ito4CtspParticle bestParticle;// 当代粒子群中最好解
    private Ito4CtspParticle allBestParticle;// 全局最好解

    /**
     * 初始化
     */
    private void init() {
        T = T0;// 初始化环境温度
        bestParticle = new Ito4CtspParticle();// 当代粒子群中最好解
        allBestParticle = new Ito4CtspParticle();// 目前最优解

        initWeight();// 初始化各个点之间的权重，边参与情况

        particles = new Ito4CtspParticle[M];// 初始化各微粒
        for (int i = 0; i < M; i++) {
            particles[i] = new Ito4CtspParticle();
        }
    }

    /**
     * 初始化权重值
     */
    private void initWeight() {

        weight = new double[me.n][me.n];
        for (int i = 0; i < me.n; i++) {
            for (int j = 0; j < i; j++) {
                if (me.canVisit(i, j)) {
                    weight[i][j] = INITW;
                    weight[j][i] = weight[i][j];
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
        double avgW = 0;
        /*-------------计算信息素浓度平均值--------------*/
        for (int i = 0; i < me.n; i++) {
            for (int j = 0; j < i; j++) {
                sum += weight[i][j];
            }
        }
        // TODO 除数是否需要优化？？？
        avgW = (sum + 0.0) / (me.n * (me.n - 1) * 0.5);
        // System.out.println(avgW + "\t" + m_nAnts[index].alfa + "\t" + T
        // + "\t" + allBestParticle.fitness + "\t"
        // + allBestParticle.disLen);

		/*-------------将路径分级--------------*/
        int[][] g = new int[me.n][me.n];

        for (int i = 0, j, k; i < particles[index].tour.size() - 1; i++) { // 当前解
            j = particles[index].tour.get(i);
            k = particles[index].tour.get(i + 1);
            if (j == k)
                continue;
            g[j][k] += 1;
            g[k][j] += 1;
        }
        for (int i = 0, j, k; i < bestParticle.tour.size() - 1; i++) { // 当前最优解
            j = bestParticle.tour.get(i);
            k = bestParticle.tour.get(i + 1);
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
                    weight[i][j] += (particles[index].getAlfa());
                    if (weight[i][j] > 3 * avgW) {
                        weight[i][j] = 3 * avgW;
                    }
                } else if (g[i][j] == 0) {
                    if (p > Math.random()) {
                        weight[i][j] += (particles[index].getAlfa());
                        if (weight[i][j] > 3 * avgW) {
                            weight[i][j] = 3 * avgW;
                        }
                    }
                }
            }
        }
    }

    /**
     * 更新所有粒子的路径
     */
    private void updateAllPath() {
        /*-------------信息素浓度以0.9的倍率挥发--------------*/
        for (int i = 0; i < me.n; i++) {
            for (int j = 0; j < me.n; j++) {
                if (!me.canVisit(i, j)) {
                    weight[i][j] = 0;
                    continue;
                }
                weight[i][j] *= 0.9;
                if (weight[i][j] < 0.01) {
                    weight[i][j] = 0.01;
                }
            }
        }
        /*---------所有微粒更新信息素浓度，然后构造新的路径---------*/
        for (int i = 0; i < particles.length; i++) {
            updateWeight(particles.length - 1 - i);

            particles[particles.length - 1 - i].updatePath(weight);
        }
    }

    /**
     * 环境温度随迭代不断降低，以使微粒趋于稳定
     */
    private void updateTemperature() {
        if (curGen % TLength == 0) {
            T *= ρ;
            if (T < 1) {
                T = 1;
            }
        }
    }

    /**
     * 基于排序计算每个粒子的半径
     */
    private void updateAllRadius() {
        for (int i = 0; i < particles.length; i++) {
            particles[i].updateRadius((1.0 / M) * i + PRECISION);
        }
    }

    private void updateAllAlfa() {
        for (int i = 0; i < particles.length; i++) {
            particles[i].updateAlfa(T);
        }
    }

    /**
     * 找到当代的最好解和最差解
     */
    private void updateBestAndNoUpdateTime() {
        /*-------已经对粒子群按照适应度值进行降序排序，所有最后一个为最优解-------*/
        particles[particles.length - 1].copyTo(bestParticle);

		/*-------更新全局最优解-------*/
        if (allBestParticle.fitness > bestParticle.fitness) {
            bestParticle.copyTo(allBestParticle);
            noUpdateTime = 0;
            noUpdateTime2 = 0;
        } else {
            noUpdateTime++;
            noUpdateTime2++;
            if (noUpdateTime >= 20) {
                initWeight();
                noUpdateTime = 0;
            } else if (noUpdateTime2 >= 60) {
                curGen = GEN;
            }
        }
    }

    @Override
    public void execute() {
        init();
        // LinkedList<Double> bestlist = new LinkedList<>();
        int count = 0;
        for (curGen = 1; curGen <= GEN; curGen++) {
            count++;
            /*-------更新所有粒子的路径-------*/
            updateAllPath();
            // climping(); // 采用爬山法作为局部波动,更新种群
            /*-----对粒子群进行排序，按距离从大到小-------*/
            Arrays.sort(particles);
            /*------找到本轮迭代的最优解和全局最优解------*/
            updateBestAndNoUpdateTime();

			/*-------更新各微粒半径-------*/
            updateAllRadius();// 基于排序的方法
            /*-----更新环境温度-------*/
            updateTemperature();
			/*------更新运动能力------*/
            updateAllAlfa();
            // bestlist.add(allBestParticle.fitness);
        }
    }

    @Override
    public double getBestValue() {
        return allBestParticle.fitness;
    }

    @Override
    public List<Integer> getBestTour() {
        return allBestParticle.tour;
    }

}
