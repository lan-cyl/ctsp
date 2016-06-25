package com.b502lab.ctsp.common;

/**
 * 求解距离的方法：欧氏距离、地理距离等
 * 
 * @author yonglecai
 *
 */
public class DistanceUtil {

	/**
	 * 计算距离
	 */
	public static double calDistance(CityNode[] CITY_INFO, int i, int j, String flag) {
		if (i == j)
			return 0;

		double x1 = CITY_INFO[i].x;
		double y1 = CITY_INFO[i].y;
		double x2 = CITY_INFO[j].x;
		double y2 = CITY_INFO[j].y;
		double dis = 0;

		switch (flag) {
		case "EUC_2D":
			dis = roundDistance(x1, y1, x2, y2);
			break;
		case "CEIL_2D":
			dis = ceilDistance(x1, y1, x2, y2);
			break;
		case "GEO":
			dis = geoDistance(x1, y1, x2, y2);
			break;
		case "ATT":
			dis = attDistance(x1, y1, x2, y2);
			break;
		default:
			System.err.println(
					"What the fuck!!!\nPlease tell me which distance compute method you used!(EUC_2D,CEIL_2D,GEO,ATT)");
			break;
		}

		return dis;
	}

	/**
	 * 下限取整，去掉double型数据的小数部分，返回
	 * 
	 * @param x
	 * @return x的下限整数
	 */

	private static double dtrunc(double x) {

		int k = (int) x;
		x = (double) k;
		return x;
	}

	/**************************************************/
	/* FUNCTION: 下面四个函数实现不同的计算TSPLIB实例的距离方法 */
	/* INPUT: 两个客户点的索引 */
	/* OUTPUT: 两个客户点之间的距离 */
	/* COMMENTS: 具体的距离计算的定义请看 TSPLIB */
	/**************************************************/

	/**
	 * Weights are Euclidean distance in 2-D // 二维欧几里得距离
	 * 
	 * @param i
	 *            一个客户点的索引
	 * @param j
	 *            另一个客户点的索引
	 * @return distance between the two nodes
	 */
	private static double roundDistance(double x1, double y1, double x2, double y2) {
		double xd = x1 - x2;
		double yd = y1 - y2;
		double r = Math.sqrt(xd * xd + yd * yd);

		return r;
	}

	/**
	 * Weights are Euclidean distance in 2-D rounded up.// 二维欧几里得距离上限取整
	 * 
	 * @param i
	 *            一个客户点索引
	 * @param j
	 *            另一个客户点索引
	 * @return distance between the two nodes
	 */
	private static int ceilDistance(double x1, double y1, double x2, double y2) {
		double xd = x1 - x2;
		double yd = y1 - y2;
		double r = Math.sqrt(xd * xd + yd * yd); // 上限取整
		double t = dtrunc(r);
		if (t < r) {
			return (int) (t + 1);
		} else {
			return (int) t;
		}
	}

	/**
	 * FUNCTION: Weights are geographical distances.// 计算的是地理位置上的距离
	 * 
	 * @param i
	 *            一个客户点索引
	 * 
	 * @param j
	 *            另一个客户点索引
	 * @return distance between the two nodes
	 */
	private static int geoDistance(double x1, double y1, double x2, double y2) {
		double deg, min;
		double lati, latj, longi, longj;

		deg = dtrunc(x1); // 得到x1的整数部分，即度数
		min = x1 - deg; // 得到min的小数部分，即分
		lati = Math.PI * (deg + 5.0 * min / 3.0) / 180.0; // 计算得到纬度
		deg = dtrunc(x2);
		min = x2 - deg;
		latj = Math.PI * (deg + 5.0 * min / 3.0) / 180.0;

		deg = dtrunc(y1);
		min = y1 - deg;
		longi = Math.PI * (deg + 5.0 * min / 3.0) / 180.0; // 计算得到经度
		deg = dtrunc(y2);
		min = y2 - deg;
		longj = Math.PI * (deg + 5.0 * min / 3.0) / 180.0;

		double q1 = Math.cos(longi - longj);
		double q2 = Math.cos(lati - latj);
		double q3 = Math.cos(lati + latj);
		double dij = 6378.388 * Math.acos(0.5 * ((1.0 + q1) * q2 - (1.0 - q1) * q3)) + 1.0;
		return (int) dij;

	}

	/**
	 * FUNCTION: Special distance function for problems att48 and att532. The
	 * edge weight type ATT corresponds to a special "pseudo-Enclidean" distance
	 * function.// 专门用于计算问题att48和att532的距离计算方法
	 * 
	 * @param i
	 *            一个客户点索引
	 * @param j
	 *            另一个客户点索引
	 * @return distance between the two nodes
	 */
	private static int attDistance(double x1, double y1, double x2, double y2) {
		double xd = x1 - x2;
		double yd = y1 - y2;
		double rij = Math.sqrt((xd * xd + yd * yd) / 10.0);
		double tij = dtrunc(rij);

		if (tij < rij) {
			return (int) (tij + 1);
		} else {
			return (int) tij;
		}
	}
}