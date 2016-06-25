package com.b502lab.ctsp.ga;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.b502lab.ctsp.Method4Ctsp;
import com.b502lab.ctsp.common.Problem;

/**
 * 带模拟退火的伊藤算法求解ctsp
 * 
 * @author yonglecai
 *
 */
public class HcGa4Ctsp implements Method4Ctsp {
	/* GA 算法的参数 */
	private static final double uniformRate = 0.5; // 交叉概率
	private static final double mutationRate = 0.015; // 突变概率
	private static final int tournamentSize = 5; // 淘汰数组的大小
	private static final boolean elitism = true; // 精英主义

	/*-----种群的参数信息-----*/
	/** 群体中的微粒数 */
	private final int M = 50;
	/** 最大迭代次数 */
	private final int GEN = 20000;
	/** 算法的连续未更新迭代次数 */
	private int noUpdateTime;
	private int noUpdateTime2;
	/** 当前进化代数 */
	public int curGen = 1;

	/** 问题实例的全局变量定义 */
	public Problem problem;
	/** 双端染色体群 */
	private Individual particles[];
	/** 当代双端染色体群中最好解 */
	private Individual bestParticle;
	/** 全局最好解 */
	private Individual allBestParticle;

	/**
	 * 初始化
	 */
	private void init(File inputFile) {

		problem = new Problem(inputFile);// 读取测试算例文件

		particles = new Individual[M];// 初始化各微粒
		for (int i = 0; i < M; i++) {
			particles[i] = new Individual(problem.CITY_COUNT - 1, problem, true);
		}

		bestParticle = new Individual(problem.CITY_COUNT - 1, problem);// 当代粒子群中最好解

		allBestParticle = new Individual(problem.CITY_COUNT - 1, problem);// 目前最优解
	}

	/**
	 * 找到当代的最好解和最差解
	 */
	private void findTheBest() {
		int minIndex = 0;
		for (int i = 1; i < particles.length; i++) {
			if (particles[i].fitness < particles[minIndex].fitness)
				minIndex = i;
		}
		// 对最优解做进一步开发
//		particles[minIndex].hillClambing(problem);
		bestParticle = particles[minIndex].clone();

		/*-------更新全局最优解-------*/
		if (allBestParticle.fitness > bestParticle.fitness) {
			allBestParticle = bestParticle.clone();
			noUpdateTime = 0;
			noUpdateTime2 = 0;
		} else {
			noUpdateTime++;
			noUpdateTime2++;
			if (noUpdateTime >= 20) {
				// initDistance();
				noUpdateTime = 0;
			} else if (noUpdateTime2 >= 60) {
				// curGen = GEN;
			}
		}
	}

	// Selects candidate tour for crossover
	private Individual tournamentSelection() {
		// Get the fittest tour
		Individual fittest = new Individual();
		// For each place in the tournament get a random candidate tour and
		// add it
		for (int i = 0; i < tournamentSize; i++) {
			int randomId = (int) (Math.random() * M);
			if (fittest.fitness > particles[randomId].fitness) {
				fittest = particles[randomId].clone();
			}
		}
		return fittest;
	}

	private Individual selection(Individual parent1) {
		double sum = 0;
		double[] fitness = new double[particles.length];
		for (int i = 0; i < particles.length; i++) {
			sum += particles[i].fitness;
		}
		for (int i = 0; i < fitness.length; i++) {
			fitness[i] = particles[i].fitness / sum;
		}

		double rand = Math.random();
		sum = 0;
		for (int i = fitness.length - 1; i >= 0; i--) {
			sum += fitness[i];
			if (sum > rand)
				if (particles[i].equals(parent1))
					return particles[(i + 1) % fitness.length].clone();
				else
					return particles[i].clone();
		}
		return allBestParticle.clone();
	}

	// 进化种群
	private void evolvePopulation() {
		Individual[] newPopulation = new Individual[M];// 初始化各微粒
		for (int i = 0; i < M; i++) {
			newPopulation[i] = new Individual(problem.CITY_COUNT - 1, problem);
		}

		// 第一个位置 保持最优个体
		int elitismOffset = 0;
		if (elitism) {
			newPopulation[0] = bestParticle.clone();
			elitismOffset = 1;

			// 对最优解做进一步开发
			// newPopulation[0].hillClambing(problem);
		}

		// 种群交叉操作
		// 从当前的种群pop 来 创建下一代种群 newPopulation
		for (int i = elitismOffset; i < newPopulation.length; i++) {
			// 选择较优的parent
			Individual parent1 = selection(bestParticle);
			Individual parent2 = selection(parent1);
			// 交叉 parents
			if (!parent1.equals(parent2))
				parent1.crossOver(problem, parent2);
			// 将产生的child放入到 新种群 newPopulation
			newPopulation[i] = parent1;
		}

		// 进行突变操作，给基因来点good luck
		for (int i = elitismOffset; i < newPopulation.length; i++) {
			newPopulation[i].mutation(problem);
		}
		particles = newPopulation;
	}

	@Override
	public void execute(File inputFile) {
		init(inputFile);
		System.out.println("executing it, please wait a moment.....");
		for (curGen = 1; curGen <= GEN; curGen++) {

			/*------找到本轮迭代的最优解和全局最优解------*/
			findTheBest();

			evolvePopulation();
		}
		System.out.println("execute complete!!!");
		System.out.println("最优解=" + allBestParticle.fitness + "\n");
	}

	@Override
	public double getBestFitness() {
		return allBestParticle.fitness;
	}

	@Override
	public List<Integer> getBestTour() {
		List<Integer> list = new LinkedList<>();

		list.add(problem.DEPORT);
		for (int i = 1; i <= problem.SALESMAN_COUNT; i++) {
			for (int j = 0; j < allBestParticle.salesmanChromo.length; j++) {
				if (allBestParticle.salesmanChromo[j] == i) {
					list.add(allBestParticle.cityChromo[j]);
				}
			}
			list.add(problem.DEPORT);
		}
		return list;
	}

	@Override
	public Problem getProblem() {
		return problem;
	}
}
