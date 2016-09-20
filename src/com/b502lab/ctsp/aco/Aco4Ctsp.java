package com.b502lab.ctsp.aco;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.b502lab.ctsp.Method4Ctsp;
import com.b502lab.ctsp.common.Problem;

/**
 * aco求解ctsp
 * 
 * @author yonglecai
 *
 */
public class Aco4Ctsp implements Method4Ctsp {

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

	/*-----种群的参数信息-----*/
	/** 群体中的微粒数 */
	private final int M = 50;
	/** 最大迭代次数 */
	private final int GEN = 4000;

	/** 各边的初始信息素浓度 */
	private final int INITW = 1;

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
	private Ant particles[];
	/** 当代粒子群中最好解 */
	private Ant bestParticle;
	/** 全局最好解 */
	private Ant allBestParticle;

	/**
	 * 初始化
	 */
	private void init(File inputFile) {

		/*---------当代粒子群中最好解---------*/
		bestParticle = new Ant();

		/*---------目前最优解---------*/
		allBestParticle = new Ant();

		/*---------读取测试算例文件---------*/
		this.problem = new Problem(inputFile);

		/*---------初始化各个点之间的距离，权重，边参与情况---------*/
		problem.initCityDistance();
		initWeight();

		/*---------初始化各微粒---------*/
		particles = new Ant[M];
		for (int i = 0; i < M; i++) {
			particles[i] = new Ant();
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

	private void evaporatePheromone() {
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
	}

	private void releasePheromone() {
		for (int i = 0; i < particles.length; i++) {
			particles[i].releasePheromone(weight);
		}
	}

	/**
	 * 更新所有粒子的路径
	 */
	private void updateAllPath() {

		/*---------所有微粒更新信息素浓度，然后构造新的路径---------*/
		for (int i = 0; i < particles.length; i++) {
			particles[particles.length - 1 - i].constructSolution(problem, weight);
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
			// 蒸发信息素
			evaporatePheromone();
			// 释放信息素
			releasePheromone();

			/*-------更新所有粒子的路径-------*/
			updateAllPath();
			/*------找到本轮迭代的最优解和全局最优解------*/
			updateBestAndNoUpdateTime();

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
