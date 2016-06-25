package com.b502lab.ctsp.ga;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.b502lab.ctsp.common.Problem;

/**
 * 双端染色体结构
 * 
 * @author yonglecai
 *
 */
public class Individual2 implements Comparable<Individual2> {
	/** 总适应度值：包括(路程费用) */
	public double fitness;

	// 解
	public int[] cityChromo;
	public int[] salesmanChromo;

	public Individual2() {
		super();
		fitness = 1.0e10;
	}

	public Individual2(int cityCount, Problem problem) {
		fitness = 1.0e10;
		cityChromo = new int[cityCount];
		salesmanChromo = new int[cityCount];
	}

	public Individual2(int cityCount, Problem problem, boolean useGreedy) {
		this(cityCount, problem);
		init(problem);
		if (useGreedy)
			greedy(problem);
	}

	private void init(Problem problem) {
		// 随机生成cityChromo
		int n = cityChromo.length;
		LinkedList<Integer> list = new LinkedList<>();
		for (int i = 1; i <= n; i++)
			list.add(i);
		for (int i = 0; i < n; i++) {
			int t = (int) (Math.random() * list.size());
			cityChromo[i] = list.get(t);
			list.remove(t);
		}
		// 随机生成salesmanChromo
		int salesmanCount = problem.SALESMAN_COUNT;
		list = new LinkedList<>();
		for (int i = 1; i <= salesmanCount; i++)
			list.add(i);
		for (int i = 0; i < n; i++) {
			int t = (int) (Math.random() * list.size());
			salesmanChromo[i] = list.get(t);
		}
		adjust(problem);
		this.fitness = calculateFitness(problem);

	}

	private void greedy(Problem problem) {
		int n = cityChromo.length;

		// greedy
		for (int i = 0; i < n - 1; i++) {
			int k = i + 1;
			double t = problem.CITY_DISTANCE[cityChromo[i]][cityChromo[k]];
			for (int j = 0; j < n && j != i; j++) {
				if (problem.CITY_DISTANCE[cityChromo[i]][cityChromo[j]] < t) {
					k = j;
					t = problem.CITY_DISTANCE[cityChromo[i]][cityChromo[k]];
				}
			}
			// swap k,i+1
			int tmp = cityChromo[k];
			cityChromo[k] = cityChromo[i + 1];
			cityChromo[i + 1] = tmp;
			tmp = salesmanChromo[k];
			salesmanChromo[k] = salesmanChromo[i + 1];
			salesmanChromo[i + 1] = tmp;
		}
	}

	private int nearestCity(Problem problem, int curCity, LinkedList<Integer> list) {
		int k = 0;

		for (int j = 2; j < list.size(); j++) {
			if (problem.CITY_DISTANCE[curCity][list.get(j)] < problem.CITY_DISTANCE[curCity][list.get(k)]) {
				k = j;
			}
		}
		return k;
	}

	private void init(Problem problem, int firstCity) {
		int n = cityChromo.length;

		// 贪心策略生成cityChromo
		LinkedList<Integer> list = new LinkedList<>();
		for (int i = 1; i <= n; i++)
			list.add(i);
		cityChromo[0] = firstCity % n;
		list.remove(cityChromo[0] == 0 ? 0 : cityChromo[0] - 1);
		for (int i = 1; i < n; i++) {
			int t = nearestCity(problem, cityChromo[i - 1], list);
			cityChromo[i] = list.get(t);
			list.remove(t);
		}
		// 随机生成salesmanChromo
		int salesmanCount = problem.SALESMAN_COUNT;
		list = new LinkedList<>();
		for (int i = 1; i <= salesmanCount; i++)
			list.add(i);
		for (int i = 0; i < n; i++) {
			int t = (int) (Math.random() * list.size());
			salesmanChromo[i] = list.get(t);
		}

		adjust(problem);
		this.fitness = calculateFitness(problem);
	}

	/**
	 * 用最优染色体结构来调整一下我自己，漂移也就是跟最优解交叉：交叉长度由漂移强度决定，交叉起点随机选择，交叉后无论结果如何都要接收:)
	 * ???是不是可以生两个胖娃娃，先生一个看看效果==
	 * 
	 * xxx交叉后重复问题
	 * 
	 * @param child2
	 */
	public void crossOver(Problem problem, Individual2 child2) {
		int[] cityChromo_back = cityChromo.clone();
		int[] salesmanChromo_back = salesmanChromo.clone();
		int[] cityChromo2_back = child2.cityChromo.clone();
		int[] salesmanChromo2_back = child2.salesmanChromo.clone();
		int n = cityChromo.length;
		int t = (int) (Math.random() * n);// 交叉起点
		int len = (int) (Math.random() * n);// 交叉长度
		HashMap<Integer, Integer> map1 = new HashMap<>();
		HashMap<Integer, Integer> map2 = new HashMap<>();
		for (int i = 0; i < len; i++) {
			int a = (t + i) % n;
			map1.put(child2.cityChromo[a], cityChromo[a]);
			map2.put(cityChromo[a], child2.cityChromo[a]);
			swapCity(child2, a);
		}
		// 处理重复值Chromo.length
		int end = (t + len - 1) % n;
		for (int i = 0; i < cityChromo.length; i++) {
			if (end < t) {
				if (i <= end || i >= t) {
					continue;
				}
			} else if ((i >= t && i <= end)) {
				continue;
			}
			while (contains(cityChromo, i, t, end)) {
				cityChromo[i] = map1.get(cityChromo[i]);
			}
			while (child2.contains(child2.cityChromo, i, t, end)) {
				child2.cityChromo[i] = map2.get(child2.cityChromo[i]);
			}
		}
		adjust(problem);

		double fitn = calculateFitness(problem);
		fitness = fitn;
//		if (fitn <= fitness) {
//		} else {
//			cityChromo = cityChromo_back;
//			salesmanChromo = salesmanChromo_back;
//		}

		child2.adjust(problem);
		fitn = child2.calculateFitness(problem);
		child2.fitness = fitn;
//		if (fitn <= child2.fitness) {
//		} else {
//			cityChromo = cityChromo2_back;
//			salesmanChromo = salesmanChromo2_back;
//		}
	}

	// 与个体child2交换第k个位置的染色体单元
	private void swapCity(Individual2 child2, int k) {
		int tmp = this.cityChromo[k];
		this.cityChromo[k] = child2.cityChromo[k];
		child2.cityChromo[k] = tmp;
	}

	private boolean contains(int[] arr, int k, int start, int end) {
		if (start <= end)
			for (int i = start; i <= end; i++) {
				if (arr[k] == arr[i]) {
					return true;
				}
			}
		else {
			for (int i = 0; i <= end; i++) {
				if (arr[k] == arr[i]) {
					return true;
				}
			}
			for (int i = start; i < arr.length; i++) {
				if (arr[k] == arr[i]) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 自己来蹦达一下，波动也就是变异：染色体把自己一段基因旋转来完成波动，旋转长度由波动强度决定，旋转起点随机选择，波动强度也作为较差解接收的概率
	 */
	public void mutation(Problem problem) {
		int[] cityChromo_back = cityChromo.clone();
		int[] salesmanChromo_back = salesmanChromo.clone();
		int n = 4;

		for (int tourPos1 = 0; tourPos1 < n; tourPos1++) {
			// Get a second random position in the tour
			int tourPos2 = (int) (n * Math.random());
			swap(tourPos1, tourPos2);
		}

		adjust(problem);
		double fitn = calculateFitness(problem);
		fitness = fitn;
		// if (fitn <= fitness) {
		// } else {
		// cityChromo = cityChromo_back;
		// salesmanChromo = salesmanChromo_back;
		// }
	}

	/**
	 * 自己来爬个山：对于m个旅行商，每个旅行商随机从自己的路径中选出两个交换一下，如果效果好就替换，然后下一个旅行商再重复
	 */
	public void hillClambing(Problem problem) {
		int MaxClambingTime = 1;// 只对一个点爬山
		for (int t = 0; t < MaxClambingTime; t++) {
			for (int i = 1; i <= problem.SALESMAN_COUNT; i++) {
				for (int j = 0; j < problem.REGION_COUNT[i - 1]; j++) {

					int[] ab = select2City(problem, i);
					swap(ab[0], ab[1]);
					double fitn = calculateFitness(problem);
					if (fitn > this.fitness) {
						swap(ab[0], ab[1]);
					} else {
						this.fitness = fitn;
					}
				}
			}
		}
	}

	private int[] select2City(Problem problem, int salesmank) {
		int n = problem.CITY_COUNT - 1;
		int a = (int) (Math.random() * n);
		int b = (int) (Math.random() * n);
		for (; salesmanChromo[a] != salesmank; a = (a + 1) % n) {
		}
		for (; salesmanChromo[b] != salesmank; b = (b + 1) % n) {
		}
		return new int[] { a, b };
	}

	public void simulatedAnnealing(Problem problem) {
		int t0 = 100, coolingTimes = 60, stepLen = 30;
		double alfa = 0.9;
		double t = t0;
		for (int i = 0; i < coolingTimes; i++) {
			for (int j = 0; j < stepLen; j++) {
				int k = (int) (Math.random() * problem.SALESMAN_COUNT) + 1;
				int[] ab = select2City(problem, k);
				swap(ab[0], ab[1]);
				double fitn = calculateFitness(problem);
				if (fitn < this.fitness)
					this.fitness = fitn;
				else if (Math.exp((this.fitness - fitn) / t) > Math.random()) {
					this.fitness = fitn;
				} else
					swap(ab[0], ab[1]);
			}
			t = t * alfa;
		}
	}

	private void swap(int a, int b) {
		int tmp = cityChromo[a];
		cityChromo[a] = cityChromo[b];
		cityChromo[b] = tmp;
	}

	/**
	 * 根据 城市染色体 调整 旅行商染色体
	 */
	public void adjust(Problem problem) {
		for (int i = 0; i < cityChromo.length; i++) {
			int k = problem.CITY_INFO[cityChromo[i]].k;
			if (k != 0)
				salesmanChromo[i] = k;
		}
	}

	public double calculateFitness(Problem problem) {
		Set<Integer> set = new HashSet<>();
		for (int i : cityChromo) {
			set.add(i);
		}
		double rst = 0;
		for (int i = 1; i <= problem.SALESMAN_COUNT; i++) {
			LinkedList<Integer> list = new LinkedList<>();
			int preCity = 0;
			list.add(preCity);
			for (int j = 0; j < cityChromo.length; j++) {
				int city = cityChromo[j];
				int man = salesmanChromo[j];
				if (man == i) {
					rst += problem.CITY_DISTANCE[preCity][city];
					preCity = city;
					list.add(preCity);
				}
			}
			rst += problem.CITY_DISTANCE[preCity][0];
			list.add(0);
		}

		return rst;
	}

	@Override
	public boolean equals(Object obj) {
		Individual2 t = (Individual2) obj;
		for (int i = 0; i < t.cityChromo.length; i++)
			if (t.cityChromo[i] != this.cityChromo[i])
				return false;
		return true;
	};

	public void swap(Individual2 ind2) {
		double tmp = this.fitness;
		this.fitness = ind2.fitness;
		ind2.fitness = tmp;

		int ttt = -1;
		for (int i = 0; i < ind2.cityChromo.length; i++) {
			ttt = this.cityChromo[i];
			this.cityChromo[i] = ind2.cityChromo[i];
			ind2.cityChromo[i] = ttt;

			ttt = this.salesmanChromo[i];
			this.salesmanChromo[i] = ind2.salesmanChromo[i];
			ind2.salesmanChromo[i] = ttt;
		}
	}

	@Override
	public String toString() {
		return "适应值为>>" + fitness + "\r\n路径选择为>>\r\n" + Arrays.toString(cityChromo) + "\r\n"
				+ Arrays.toString(salesmanChromo);
	}

	@Override
	public Individual2 clone() {
		Individual2 ind = new Individual2();
		ind.fitness = fitness;
		ind.cityChromo = cityChromo.clone();
		ind.salesmanChromo = salesmanChromo.clone();
		return ind;
	}

	@Override
	public int compareTo(Individual2 ind) {
		if (fitness < ind.fitness)
			return 1;
		else if (fitness == ind.fitness)
			return 0;
		else
			return -1;
	}
}
