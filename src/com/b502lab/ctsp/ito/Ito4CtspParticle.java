package com.b502lab.ctsp.ito;

import com.b502lab.ctsp.aco.ConstructSolution;
import com.b502lab.ctsp.common.Ctsp;
import com.b502lab.tsp.aco.AS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.b502lab.ctsp.common.Ctsp.me;
import static com.b502lab.tsp.common.Constant.*;

/**
 * ito粒子定义
 */
public class Ito4CtspParticle extends AS implements Comparable<Ito4CtspParticle> {

    private double[][] weight;

    private double alfa;// 当前微粒的运动能力
    private double radius;// 当前粒子的半径
    public int fitness;// 总适应度值
    public ArrayList<Integer> tour;// 微粒构造的调度方案

    public Ito4CtspParticle() {
        super(me.n, me.distance);
        fitness = Integer.MAX_VALUE;// 微粒的适应度值，初始化为大数
        tour = new ArrayList<>();// 该微粒的调度方案
    }

    /**
     * 根据粒子半径，温度改变微粒运动能力
     */
    public void updateRadius(double r) {
        radius = r;
    }

    public double getAlfa() {
        return alfa;
    }

    /**
     * 根据粒子半径，温度改变微粒运动能力
     */
    public void updateAlfa(double T) {
        double fr = 0, ft = 0;
        double eλ = Math.exp(-λ);

        fr = (Math.exp(-λ * radius) - eλ + 0.0) / (1.0 - eλ);
        ft = Math.exp(-1.0 / T);
        alfa = fr * ft + PRECISION;
    }

    /**
     * FunName: UpdatePath
     * <p>
     * Description: 更新路径并保存最优解,CurPar当前粒子
     */
    public void updatePath(double[][] weight) {
        this.weight = weight;
//        ArrayList<Integer> curTour = ConstructSolution.constructSolution(this);
        ArrayList<Integer> curTour = UpdateSolution.updatePath(weight);
        int curFit = computePathValue(curTour);

        /** 模拟退火算法来选择是否接受新的一代 */
//        if (curFit < fitness || Math.random() < 0.7) {
            fitness = curFit;
            tour = curTour;
//        }
    }

    // compute the length of the path
    protected int computePathValue(final List<Integer> pathVect) {
        int pathValue = 0;
        for (int i = 0; i < pathVect.size() - 1; i++) {
            pathValue += me.distance[pathVect.get(i)][pathVect.get(i + 1)];
        }
        return pathValue;
    }

    @Override
    public int stateTransitionRule(int curNode, Set<Integer> nodesToVisit) {
        double totalSum = 0.0;
        double[] tmp = new double[me.n];

		/*------- 计算各城市被选中的概率 -------*/
        for (int node : nodesToVisit) {
            tmp[node] = Math.pow(weight[curNode][node], ALFA)
                    * Math.pow(1 / (me.distance[curNode][node] + PRECISION), BETA); // 选择城市j的概率
            if (tmp[node] < PRECISION) tmp[node] = PRECISION;
            totalSum += tmp[node];
        }

		/*------- 轮盘赌算法选择下一个城市 -------*/
        double rnd = Math.random() * totalSum;
        totalSum = 0.0;
        for (int node : nodesToVisit) {
            totalSum += tmp[node];
            if (totalSum > rnd) {
                return node;
            }
        }
        return 0;
    }

    /**
     * 按照适应值的大小从大到小排序
     */
    @Override
    public int compareTo(Ito4CtspParticle par) {
        if (fitness < par.fitness)
            return 1;
        else if (fitness == par.fitness)
            return 0;
        else
            return -1;
    }

    public void copyTo(Ito4CtspParticle toPar) {
        toPar.alfa = alfa;
        toPar.fitness = fitness;
        toPar.tour = (ArrayList<Integer>) tour.clone();
    }
}