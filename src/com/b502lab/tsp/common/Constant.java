/*
 * Copyright (C) 2007-2016 Ugo Chirico
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Affero GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.b502lab.tsp.common;

import java.util.Random;

public class Constant {

    public static final Random RANDOM_GEN = new Random(System.currentTimeMillis());
    public static final String ALL_TSP_PATH = "ALL_tsp/";
    public static final String ALL_TSP_RESULT_PATH = "ALL_tsp/result/";
    public static final String ALL_CTSP_PATH = "ALL_ctsp/";
    public static final String ALL_CTSP_RESULT_PATH = "ALL_ctsp/result/";

    // 蚁群算法参数
    public static final int nAnts = 30;// 蚁群规模
    public static final int nASIterations = 5500;// 迭代次数
    public static final double B = 2;// 距离因子的权重
    public static final double Q0 = 0.8;// 开采的概率，下一城市为概率最大的值
    public static final double R = 0.1;// 局部更新时：信息素浓度挥发因子
    public static final double A = 0.1;// 全局更新时：信息素浓度挥发因子

    // 遗传算法参数
    public static final int nGAIterations = 25000;// 迭代次数
    public static final int POPULATION_SIZE = 40;// 种群规模
    public static final double CROSSOVER_PROBABILITY = 0.75;// 交叉概率
    public static final double MUTATION_PROBABILITY = 0.1;// 变异概率

    // 伊藤算法参数
    public static final double PRECISION = 1e-20;// 精度值
    public static final double λ = 1.4;// 求运动强度用到的参数
    public static final int ALFA = 5;// 边权重重要性因子
    public static final int BETA = 3;// 距离重要性因子
    public static final double p = 0.3;// 选取随机路径的概率

    public static final int TLength = 2;// 退火表长度
    public static final double ρ = 0.99;// 退火速率

    public static final int M = 50;// 群体中的微粒数
    public static final int GEN = 4000;// 最大迭代次数

    public static final int INITW = 1;// 各边的初始信息素浓度
    public static final double T0 = 1000;// 环境温度
}
