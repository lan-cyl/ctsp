package com.b502lab.ctsp.common;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.b502lab.ctsp.draw.CtspResultFrame;

/**
 * 保存计算结果
 *
 * @author yonglecai
 */
public class ResultWriter {
    public static void write(String filePath, List<Integer> bestTour, double bestFitness,
                             double worstFitness, double avgFitness, long avgBestEpoch, double avgBestTime, double avgTime, boolean drawOrNot) {
        try (FileWriter out = new FileWriter(filePath)) {
            out.write("\r\n最优解为 ->" + bestFitness);// 保存最优解
            out.write("\r\n最差解为 ->" + worstFitness);
            out.write("\r\n平均解为 ->" + avgFitness);
            out.write("\r\n平均求解时间为 ->" + avgTime);
            out.write("\r\n平均求得最优解时间为->" + avgBestTime);
            out.write("\r\n平均求得最优解迭代次数为->" + avgBestEpoch);
            out.write("\r\n\r\n对应的访问路径为 ->\r\n" + bestTour.toString());

            double[] x = new double[bestTour.size()];
            double[] y = new double[bestTour.size()];
            for (int i = 0; i < x.length; i++) {
                x[i] = Ctsp.me.position[bestTour.get(i)][0];
                y[i] = Ctsp.me.position[bestTour.get(i)][1];
            }
            out.write("\r\n对应的访问坐标为 ->\r\n" + Arrays.toString(x) + "\r\n" + Arrays.toString(y));

            // 画出来
            if (drawOrNot) {
                new CtspResultFrame(filePath, x, y);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void write(String filePath, double avgFitness, double bestFitness, int[] cityChromo,
                             int[] salesmanChromo) {
        try (FileWriter out = new FileWriter(filePath)) {
            out.write("平均解为->" + avgFitness);// 输出平均路径长度
            out.write("最优解为->" + bestFitness);// 输出最优路径长度
            out.write("对应的访问路径为->" + Arrays.toString(cityChromo));// 输出最优路径
            out.write("对应的访问路径为->" + Arrays.toString(salesmanChromo));// 输出最优路径
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
