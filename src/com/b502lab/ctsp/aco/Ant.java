package com.b502lab.ctsp.aco;

import java.util.LinkedList;

import com.b502lab.ctsp.common.Problem;

public class Ant {

	// 访问顺序表
	public LinkedList<Integer> tour;
	// 总的距离
	public double fitness;

	Ant() {
		fitness = 1e10;// 微粒的适应度值，初始化为大数
		// 给访问顺序表分配空间
		tour = new LinkedList<Integer>();
	}

	private int chooseCity(Problem problem, double[][] weight, int curNode, int[] visited, int flag,
			boolean flagIsOver) {
		double totalSum = 0.0;
		double[] tmp = new double[problem.CITY_COUNT];

		/*------- 计算各城市被选中的概率 -------*/
		for (int j = 1; j < problem.CITY_COUNT; j++) {
			// System.out.println("curTime=" + curTime);
			if (visited[j] != 0 || !problem.canVisit(curNode, j)
					|| (problem.CITY_INFO[j].k != 0 && flag != 0 && problem.CITY_INFO[j].k != flag)) {// 已服务过或不能访问
				tmp[j] = 0.0;
			} else {
				tmp[j] = Math.pow(weight[curNode][j], Aco4Ctsp.ALFA)
						* Math.pow(1 / (problem.CITY_DISTANCE[curNode][j] + Aco4Ctsp.PRECISION), Aco4Ctsp.BETA); // 选择城市j的概率
			}
			totalSum += tmp[j];
		}
		if (flagIsOver) {// 可以返回了
			tmp[0] = Math.pow(weight[curNode][0], Aco4Ctsp.ALFA)
					* Math.pow(1 / (problem.CITY_DISTANCE[curNode][0] + Aco4Ctsp.PRECISION), Aco4Ctsp.BETA); // 选择城市j的概率
			totalSum += tmp[0];
		}
		if (totalSum < Aco4Ctsp.PRECISION) {
			return 0;
		}
		for (int j = 0; j < problem.CITY_COUNT; j++) {// 客户点j被选中的相对概率
			tmp[j] = tmp[j] / totalSum;
		}

		/*------- 轮盘赌算法选择下一个城市 -------*/
		double rnd = Math.random();
		totalSum = 0.0;
		for (int j = 1; j < problem.CITY_COUNT; j++) {
			totalSum += tmp[j];
			if (rnd < totalSum && problem.canVisit(curNode, j)) {
				return j;
			}
		}
		return 0;
	}

	public void constructSolution(Problem problem, double[][] weight) {

		/*-------微粒的当前状态变量-------*/
		double curFit = 0;// 当前微粒的适应度值
		int curCity = 0;// 当前微粒所处位置
		LinkedList<Integer> curTour = new LinkedList<Integer>();// 当前微粒构造的调度方案

		int nextCity;// 下一个客户点
		int[] visited = new int[problem.CITY_COUNT];
		visited[0] = 1;// 仓库点无需服务

		while (!overVisited(visited)) {

			/*--------分配一辆新车开始服务---------*/
			curFit += problem.CITY_DISTANCE[curCity][problem.DEPORT];// 增加适应度值
			curCity = problem.DEPORT;// 车辆从仓库开始出发
			curTour.add(curCity);// 车辆从仓库开始出发

			int flag = 0;// 表示当前旅行商选择访问的区域编号
			boolean flagIsOver = false;
			int count = 0;
			/*--------构造路径，直到该车辆只能选仓库---------*/
			while ((nextCity = chooseCity(problem, weight, curCity, visited, flag, flagIsOver)) != 0) {
				if (flag == 0 && problem.CITY_INFO[nextCity].k != 0)
					flag = problem.CITY_INFO[nextCity].k;
				curTour.add(nextCity);
				visited[nextCity] = 1;

				curFit += problem.CITY_DISTANCE[curCity][nextCity];
				curCity = nextCity;
				if (flag != 0 && curCity != 0 && problem.CITY_INFO[curCity].k == flag) {
					count++;
					if (count == problem.REGION_COUNT[flag - 1])
						flagIsOver = true;
				}
			}
		}
		curTour.add(problem.DEPORT);
		curFit += problem.CITY_DISTANCE[curCity][problem.DEPORT];

		/** 模拟退火算法来选择是否接受新的一代 */
		if (curFit < fitness || Math.random() < 0.7) {
			fitness = curFit;
			tour = (LinkedList<Integer>) curTour.clone();
		}
	}

	/**
	 * 判断CurPar粒子所有的客户点是否都访问过
	 * 
	 * @param CurPar
	 *            微粒群中第CurPar个微粒
	 * @return 如果第CurPar个微粒已经完成对所有客户点的访问，则返回true；否则返回false
	 */
	private boolean overVisited(int[] visited) {
		for (int j = 1; j < visited.length; j++) {
			if (visited[j] == 0) {
				return false;
			}
		}
		return true;
	}

	public void releasePheromone(double[][] weight) {

		// 释放信息素的大小
		double t = 1 / fitness;
		// 释放信息素
		for (int i = 1; i < tour.size() - 1; i++) {
			int a = tour.get(i);
			int b = tour.get(i + 1);
			weight[a][b] += t;
			weight[b][a] += t;
		}
	}

	public void copyTo(Ant toPar) {
		toPar.fitness = fitness;
		toPar.tour = (LinkedList<Integer>) tour.clone();
	}
}
