package com.b502lab.ctsp;

import java.io.File;
import java.util.List;

import com.b502lab.ctsp.common.Problem;

public interface Method4Ctsp {
	/**
	 * 对inputFile表示的测试算例进行求解
	 * 
	 * @param inputFile
	 *            测试算例文件
	 */
	public void execute(File inputFile);

	/**
	 * 得到求解的问题定义
	 * 
	 * @return
	 */
	public Problem getProblem();

	/**
	 * 得到最好解
	 * 
	 * @return
	 */
	public double getBestFitness();

	/**
	 * 得到最好解的路径
	 * 
	 * @return
	 */
	public List<Integer> getBestTour();
}
