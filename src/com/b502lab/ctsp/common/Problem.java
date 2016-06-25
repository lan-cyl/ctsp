package com.b502lab.ctsp.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 * VRP问题定义，包含有问题名称、城市个数、城市信息、城市之间的距离信息、旅行商个数、独有城市和共享城市集合
 * 
 * @author yonglecai
 *
 */
public class Problem {
	/** 实例名称 */
	public String INSTANCE_NAME;

	// 旅行商个数
	public int SALESMAN_COUNT;

	// 各个区域的大小
	public int[] REGION_COUNT;

	/** 实例规模即城市个数 */
	public int CITY_COUNT;

	/** 客户信息 */
	public CityNode[] CITY_INFO;

	/** 距离,存储各客户点之间的距离 */
	public double[][] CITY_DISTANCE;// 如果两个城市不能被一个旅行商访问，则距离为无穷大

	/** 仓库位置 */
	public final int DEPORT = 0;

	public String DISTANCE_TYPE = "EUC_2D";

	public Problem(File filePath) {
		readProblem(filePath);
		initCityDistance();
	}

	private void readProblem(File inputFile) {
		try (Scanner sc = new Scanner(new FileReader(inputFile))) {

			System.out.println("Reading " + inputFile);

			/*---------读取问题名称、分组个数、每个组的点数---------*/
			String name = sc.nextLine().trim().split(":")[1].trim();
			INSTANCE_NAME = name;

			String comment = sc.nextLine();
			String type = sc.nextLine();

			String dimension = sc.nextLine().trim().split(":")[1].trim();
			CITY_COUNT = Integer.parseInt(dimension);

			String[] salesmen = sc.nextLine().trim().split(":")[1].trim().split("\\(");
			SALESMAN_COUNT = Integer.parseInt(salesmen[0]);
			REGION_COUNT = new int[SALESMAN_COUNT + 1];// 各个分组的城市个数
			String[] tmp = salesmen[1].substring(0, salesmen[1].length() - 1).split(",");
			for (int i = 0; i < REGION_COUNT.length; i++) {
				REGION_COUNT[i] = Integer.parseInt(tmp[i]);
			}
			REGION_COUNT[REGION_COUNT.length - 1] -= 1;

			String EDGE_WEIGHT_TYPE = sc.nextLine().trim().split(":")[1].trim();
			// 读取距离类型(EUC_2D,CEIL_2D,GEO,ATT)
			DISTANCE_TYPE = EDGE_WEIGHT_TYPE;

			String DISPLAY_DATA_TYPE = sc.nextLine();
			String NODE_COORD_SECTION = sc.nextLine();

			/*---------读取每个客户的信息---------*/
			int indx = 0;
			CityNode[] customerNode = new CityNode[CITY_COUNT];
			customerNode[indx] = new CityNode();
			sc.next();
			customerNode[indx].x = sc.nextDouble();
			customerNode[indx].y = sc.nextDouble();
			customerNode[indx++].k = 0;
			for (int i = 0; i < REGION_COUNT.length; i++) {
				int kk = (i + 1) % REGION_COUNT.length;
				for (int j = 0; j < REGION_COUNT[i]; j++) {
					customerNode[indx] = new CityNode();
					sc.next();
					customerNode[indx].x = sc.nextDouble();
					customerNode[indx].y = sc.nextDouble();
					customerNode[indx++].k = kk;
				}
			}
			CITY_INFO = customerNode;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void initCityDistance() {
		this.CITY_DISTANCE = new double[CITY_COUNT][CITY_COUNT];
		for (int i = 0; i < CITY_INFO.length; i++) {
			for (int j = 0; j <= i; j++) {
				double dis = 1e10;// 如果两个城市不能被一个旅行商访问，则距离为无穷大
				if (canVisit(i, j)) {
					dis = DistanceUtil.calDistance(CITY_INFO, i, j, DISTANCE_TYPE);
				}
				CITY_DISTANCE[i][j] = dis;
				CITY_DISTANCE[j][i] = dis;
			}
		}
	}

	public boolean canVisit(int i, int j) {
		int a = CITY_INFO[i].k;
		int b = CITY_INFO[j].k;
		return (a == 0 || b == 0 || a == b);
	}

	public static void main(String[] args) {
		Problem pro = new Problem(new File("newDataset/gr229-10"));
		print(pro);
	}

	private static void print(Problem pro) {
		System.out.print("x" + "=[");
		for (int k = 0; k <= pro.SALESMAN_COUNT; k++) {
			for (int j = 0; j < pro.CITY_INFO.length; j++) {
				if (pro.CITY_INFO[j].k == k) {
					System.out.print(pro.CITY_INFO[j].x + ",");
				}
			}
			System.out.println(";");
		}
		System.out.println("]");

		System.out.print("y" + "=[");
		for (int k = 0; k <= pro.SALESMAN_COUNT; k++) {
			for (int j = 0; j < pro.CITY_INFO.length; j++) {
				if (pro.CITY_INFO[j].k == k) {
					System.out.print(pro.CITY_INFO[j].y + ",");
				}
			}
			System.out.println(";");
		}
		System.out.println("]");
	}
}