package com.b502lab.ctsp.ito;

import java.util.ArrayList;

import static com.b502lab.ctsp.common.Ctsp.me;
import static com.b502lab.tsp.common.Constant.*;

/**
 * ito粒子定义
 */
public class UpdateSolution {

    private static boolean overVisited(int[] visited) {
        for (int j = 1; j < visited.length; j++) {
            if (visited[j] == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * FunName: UpdatePath
     * <p>
     * Description: 更新路径
     */
    public static ArrayList<Integer> updatePath(double[][] weight) {
        ArrayList<Integer> curTour = new ArrayList<>();
        /*-------微粒的当前状态变量-------*/
        int curCity = 0;// 当前微粒所处位置

        int nextCity;// 下一个客户点
        int[] visited = new int[me.n];
        visited[0] = 1;// 仓库点无需服务

        while (!overVisited(visited)) {

			/*--------分配一辆新车开始服务---------*/
            curCity = me.DEPORT;// 车辆从仓库开始出发
            curTour.add(curCity);// 车辆从仓库开始出发

            int flag = 0;// 表示当前旅行商选择访问的区域编号
            boolean flagIsOver = false;
            int count = 0;
            /*--------构造路径，直到该车辆只能选仓库---------*/
            while ((nextCity = nextCustomer(weight, curCity, visited, flag, flagIsOver)) != 0) {
                if (flag == 0 && me.colors[nextCity] != 0)
                    flag = me.colors[nextCity];
                curTour.add(nextCity);
                visited[nextCity] = 1;

                curCity = nextCity;
                if (flag != 0 && curCity != 0 && me.colors[curCity] == flag) {
                    count++;
                    if (count == me.colorsCount[flag])
                        flagIsOver = true;
                }
            }
        }
        curTour.add(me.DEPORT);
        return curTour;
    }

    /**
     * 采用轮盘赌方法选取下一个客户点,CurPar当前粒子，CurNode当前客户点
     *
     * @param curNode 当前微粒CurPar正在配送的客户点CurNode
     * @return 整数，代表第CurPar个微粒完成对CurNode的配送之后，下一个客户点的编号
     */
    private static int nextCustomer(double[][] weight, int curNode, int[] visited, int flag,
                                    boolean flagIsOver) {
        double totalSum = 0.0;
        double[] tmp = new double[me.n];

		/*------- 计算各城市被选中的概率 -------*/
        for (int j = 1; j < me.n; j++) {
            // System.out.println("curTime=" + curTime);
            if (visited[j] != 0 || !me.canVisit(curNode, j)
                    || (me.colors[j] != 0 && flag != 0 && me.colors[j] != flag)) {// 已服务过或不能访问
                tmp[j] = 0.0;
            } else {
                tmp[j] = Math.pow(weight[curNode][j], ALFA)
                        * Math.pow(1 / (me.distance[curNode][j] + PRECISION), BETA); // 选择城市j的概率
                if (tmp[j] < PRECISION) tmp[j] = PRECISION;
            }
            totalSum += tmp[j];
        }
        if (flagIsOver) {// 可以返回了
            tmp[0] = Math.pow(weight[curNode][0], ALFA)
                    * Math.pow(1 / (me.distance[curNode][0] + PRECISION), BETA); // 选择城市j的概率
            totalSum += tmp[0];
        }
        if (totalSum < PRECISION) {
            return 0;
        }
        for (int j = 0; j < me.n; j++) {// 客户点j被选中的相对概率
            tmp[j] = tmp[j] / totalSum;
        }

		/*------- 轮盘赌算法选择下一个城市 -------*/
        double rnd = Math.random();
        totalSum = 0.0;
        for (int j = 1; j < me.n; j++) {
            totalSum += tmp[j];
            if (rnd < totalSum && me.canVisit(curNode, j)) {
                return j;
            }
        }
        return 0;
    }
}