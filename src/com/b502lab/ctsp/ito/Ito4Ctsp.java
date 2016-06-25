package com.b502lab.ctsp.ito;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.b502lab.ctsp.Method4Ctsp;
import com.b502lab.ctsp.common.Problem;

/**
 * 原来求解vrp问题的ito求解ctsp
 * 
 * @author yonglecai
 *
 */
public class Ito4Ctsp implements Method4Ctsp {

	/** 精度值 */
	public static final double PRECISION = 1e-20;
	/** λ求运动强度用到的参数 */
	public static final double λ = 1.4;
	/** 边权重重要性因子 */
	public static final int ALFA = 5;
	/** 距离重要性因子 */
	public static final int BETA = 3;
	/** 选取随机路径的概率 */
	public static final double p = 0.3;

	/** 退火表长度 */
	public final int TLength = 2;
	/** 退火速率 */
	public final double ρ = 0.99;

	/*-----种群的参数信息-----*/
	/** 群体中的微粒数 */
	private final int M = 50;
	/** 最大迭代次数 */
	private final int GEN = 4000;

	/** 各边的初始信息素浓度 */
	private final int INITW = 1;
	/** 环境温度 */
	private final double T0 = 1000;

	/** 环境温度 */
	private double T;

	/** 算法的连续未更新迭代次数 */
	private int noUpdateTime;
	private int noUpdateTime2;
	/** 信息素浓度 */
	private double[][] weight;
	/** 当前进化代数 */
	private int curGen = 1;

	/** 问题实例的全局变量定义 */
	private Problem problem;
	/** 粒子群 */
	private Ito4CtspParticle particles[];
	/** 当代粒子群中最好解 */
	private Ito4CtspParticle bestParticle;
	/** 全局最好解 */
	private Ito4CtspParticle allBestParticle;

	/**
	 * 初始化
	 */
	private void init(File inputFile) {

		T = T0;// 初始化环境温度

		/*---------当代粒子群中最好解---------*/
		bestParticle = new Ito4CtspParticle();

		/*---------目前最优解---------*/
		allBestParticle = new Ito4CtspParticle();

		/*---------读取测试算例文件---------*/
		this.problem = new Problem(inputFile);

		/*---------初始化各个点之间的距离，权重，边参与情况---------*/
		problem.initCityDistance();
		initWeight();

		/*---------初始化各微粒---------*/
		particles = new Ito4CtspParticle[M];
		for (int i = 0; i < M; i++) {
			particles[i] = new Ito4CtspParticle();
		}
	}

	/**
	 * 初始化权重值
	 */
	private void initWeight() {

		weight = new double[problem.CITY_COUNT][problem.CITY_COUNT];
		for (int i = 0; i < problem.CITY_INFO.length; i++) {
			for (int j = 0; j <= i; j++) {
				if (problem.canVisit(i, j)) {
					weight[i][j] = INITW;
					weight[j][i] = weight[i][j];
				}
			}
		}
	}

	/**
	 * 更新权重信息
	 * 
	 * @param index
	 *            要更新权重的粒子索引
	 */
	private void updateWeight(int index) {

		double sum = 0;
		double avgW = 0;
		/*-------------计算信息素浓度平均值--------------*/
		for (int i = 0; i < problem.CITY_COUNT; i++) {
			for (int j = 0; j < problem.CITY_COUNT; j++) {
				sum += weight[i][j];
			}
		}
		// TODO 除数是否需要优化？？？
		avgW = (sum + 0.0) / (problem.CITY_COUNT * problem.CITY_COUNT);
		// System.out.println(avgW + "\t" + particles[index].alfa + "\t" + T
		// + "\t" + allBestParticle.fitness + "\t"
		// + allBestParticle.disLen);

		/*-------------将路径分级--------------*/
		int[][] g = new int[problem.CITY_COUNT][problem.CITY_COUNT];

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
		for (int i = 0; i < problem.CITY_COUNT; i++) {
			for (int j = 0; j < problem.CITY_COUNT; j++) {
				if (!problem.canVisit(i, j) || i == j)
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
		for (int i = 0; i < problem.CITY_COUNT; i++) {
			for (int j = 0; j < problem.CITY_COUNT; j++) {
				if (!problem.canVisit(i, j)) {
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
			particles[particles.length - 1 - i].updatePath(problem, weight);
		}
	}

	/** 环境温度随迭代不断降低，以使微粒趋于稳定 */
	private void updateTemperature() {
		if (curGen % TLength == 0) {
			T *= ρ;
			if (T < 1) {
				T = 1;
			}
		}
	}

	/** 基于排序计算每个粒子的半径 */
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
	public void execute(File inputFile) {
		init(inputFile);
		System.out.println("executing it, please wait a moment.....");
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
		System.out.println(count);
		System.out.println("execute complete!!!");
		System.out.println("最优解=" + allBestParticle.fitness);
		// System.out.println("收敛情况=" + bestlist + "\n");
	}

	@Override
	public double getBestFitness() {
		return allBestParticle.fitness;
	}

	@Override
	public List<Integer> getBestTour() {
		return allBestParticle.tour;
	}

	@Override
	public Problem getProblem() {
		return problem;
	}
}
