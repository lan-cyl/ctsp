package com.b502lab.ctsp.common;

import java.util.List;

public interface Base {

	/**
	 * 得到最好解
	 * 
	 * @return
	 */
	double getBestValue();

	/**
	 * 得到最好解的路径
	 * 
	 * @return
	 */
	List<Integer> getBestTour();

    void init();

	void nextEpoch();

	boolean bestUnchanged();
}
