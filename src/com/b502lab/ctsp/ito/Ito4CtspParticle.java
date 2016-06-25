package com.b502lab.ctsp.ito;

import java.util.LinkedList;

import com.b502lab.ctsp.common.Problem;

/** ito粒子定义 */
public class Ito4CtspParticle implements Comparable<Ito4CtspParticle> {

	/** 当前微粒的运动能力 */
	private double alfa;
	/** 当前粒子的半径 */
	private double radius;
	/** 总适应度值 */
	public double fitness;
	/** 微粒构造的调度方案 */
	public LinkedList<Integer> tour;

	public Ito4CtspParticle() {
		super();
		fitness = 1e10;// 微粒的适应度值，初始化为大数
		tour = new LinkedList<Integer>();// 该微粒的调度方案
	}

	/** 根据粒子半径，温度改变微粒运动能力 */
	public void updateRadius(double r) {
		radius = r;
	}

	public double getAlfa() {
		return alfa;
	}

	/** 根据粒子半径，温度改变微粒运动能力 */
	public void updateAlfa(double T) {
		double fr = 0, ft = 0;
		double eλ = Math.exp(-Ito4Ctsp.λ);

		fr = (Math.exp(-Ito4Ctsp.λ * radius) - eλ + 0.0) / (1.0 - eλ);
		ft = Math.exp(-1.0 / T);
		alfa = fr * ft + Ito4Ctsp.PRECISION;
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

	/**
	 * FunName: UpdatePath
	 * 
	 * Description: 更新路径并保存最优解,CurPar当前粒子
	 * 
	 * @param curPar
	 *            当前微粒编号
	 */
	public void updatePath(Problem problem, double[][] weight) {

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
			while ((nextCity = nextCustomer(problem, weight, curCity, visited, flag, flagIsOver)) != 0) {
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
	 * 采用轮盘赌方法选取下一个客户点,CurPar当前粒子，CurNode当前客户点
	 * 
	 * @param curPar
	 *            当前微粒编号
	 * @param curNode
	 *            当前微粒CurPar正在配送的客户点CurNode
	 * @param curTime
	 * @return 整数，代表第CurPar个微粒完成对CurNode的配送之后，下一个客户点的编号
	 */
	private int nextCustomer(Problem problem, double[][] weight, int curNode, int[] visited, int flag,
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
				tmp[j] = Math.pow(weight[curNode][j], Ito4Ctsp.ALFA)
						* Math.pow(1 / (problem.CITY_DISTANCE[curNode][j] + Ito4Ctsp.PRECISION), Ito4Ctsp.BETA); // 选择城市j的概率
			}
			totalSum += tmp[j];
		}
		if (flagIsOver) {// 可以返回了
			tmp[0] = Math.pow(weight[curNode][0], Ito4Ctsp.ALFA)
					* Math.pow(1 / (problem.CITY_DISTANCE[curNode][0] + Ito4Ctsp.PRECISION), Ito4Ctsp.BETA); // 选择城市j的概率
			totalSum += tmp[0];
		}
		if (totalSum < Ito4Ctsp.PRECISION) {
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

	/** 按照适应值的大小从大到小排序 */
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
		toPar.tour = (LinkedList<Integer>) tour.clone();
	}
}