package com.b502lab.ctsp.common;

import java.util.List;

public interface Base {
	/**
	 * 算法执行
	 */
	public void execute();

	/**
	 * 得到最好解
	 * 
	 * @return
	 */
	public double getBestValue();

	/**
	 * 得到最好解的路径
	 * 
	 * @return
	 */
	public List<Integer> getBestTour();
}
