package com.b502lab.ctsp;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.b502lab.ctsp.aco.AS4Ctsp;
import com.b502lab.ctsp.aco.ACS4Ctsp;
import com.b502lab.ctsp.aco.MMAS4Ctsp;
import com.b502lab.ctsp.ga.*;
import com.b502lab.ctsp.ito.ITO;
import com.b502lab.ctsp.ito.Ito4Ctsp;
import com.b502lab.ctsp.common.Method4Ctsp;
import com.b502lab.ctsp.common.ResultWriter;

import static com.b502lab.ctsp.common.Ctsp.me;

public class Main {
    /**
     * 测试算例文件夹
     */
    private final static String rootPath = "ALL_ctsp/";

    /**
     * 从命令行参数里得到要求解的ctsp问题名
     *
     * @param args
     * @return
     */
    private static List<File> initFilePath(String[] args) {
        File[] filesList;
        String[] filesName = null;
        if (args != null && args.length > 3) {
            filesName = new String[args.length - 3];
            for (int i = 3; i < args.length; i++)
                filesName[i - 3] = args[i];
        }

        // if no arguments input, use all test data in the root path as default.
        if (filesName == null || filesName.length == 0) {
            File file = new File(rootPath);
            filesList = file.listFiles();
        } else {// get the files want to compute.
            filesList = new File[filesName.length];
            for (int i = 0; i < filesName.length; i++) {
                String fileName = filesName[i];
                File file = new File(rootPath + fileName);
                filesList[i] = file;
            }
        }

        // remove the directory.
        List<File> list = new LinkedList<File>();
        for (File file : filesList) {
            if (!file.isDirectory() && file.getName().endsWith(".ctsp"))
                list.add(file);
        }
        return list;
    }

    /**
     * 主函数，测试实验结果
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        List<File> filesList = initFilePath(args);
        String methodName = args[1];

        execute(methodName, filesList);
    }

    private static Method4Ctsp selectMethod(String methodName) {
        Method4Ctsp method;
        switch (methodName) {
            case "Ito4Ctsp":
                method = new Ito4Ctsp();
//                method = new ITO(me.n, me.distance);
                break;
            case "GA4Ctsp":
                method = new GA4Ctsp(me.n, me.distance);
                break;
            case "GAG4Ctsp":
                method = new GAG4Ctsp(me.n, me.distance);
                break;
            case "HCGA4Ctsp":
                method = new HCGA4Ctsp(me.n, me.distance);
                break;
            case "SAGA4Ctsp":
                method = new SAGA4Ctsp(me.n, me.distance);
                break;
            case "AS4Ctsp":
                method = new AS4Ctsp(me.n, me.distance);
                break;
            case "ACS4Ctsp":
                method = new ACS4Ctsp(me.n, me.distance);
                break;
            case "MMAS4Ctsp":
                method = new MMAS4Ctsp(me.n, me.distance);
                break;
            default:
                method = new Ito4Ctsp();
                break;
        }

        return method;
    }

    private static void execute(String methodName, List<File> filesList) {

        int count = 10;// 每个算例求解次数
        for (File inputFile : filesList) {
            me.init(inputFile.getPath());
            double bestFitness = 1e10, worstFitness = 0;
            List<Integer> bestTour = null;
            double sumFit = 0;
            long startTime = System.nanoTime();

			/*------------算法执行count次，求平均值--------------*/
            for (int i = 0; i < count; i++) {
                System.out.println("\nexecuting it, please wait a moment.....");
                Method4Ctsp method = selectMethod(methodName);
                method.execute();// 算法执行
                sumFit += method.getBestValue();
                if (bestFitness > method.getBestValue()) {// 更新count次算法运行后的最好解
                    bestFitness = method.getBestValue();
                    bestTour = method.getBestTour();
                }
                if (worstFitness < method.getBestValue()) {// 更新count次算法运行后的最好解
                    worstFitness = method.getBestValue();
                }
                System.out.println("execute complete!!!");
                System.out.println("最优解=" + method.getBestValue());
                // System.out.println("收敛情况=" + bestlist + "\n");
            }
            long estimatedTime = System.nanoTime() - startTime;
            ResultWriter.write(
                    rootPath + "result/" + me.name + "." + methodName + ".opt.tour",
                    bestTour, bestFitness, worstFitness, sumFit / count,
                    estimatedTime / (count * 1e9), true);
            /*------输出最优解--------*/
            System.out.println("\n平均解为->" + sumFit / count);// 输出平均路径长度
            System.out.println("最优解为->" + bestFitness);// 输出最优路径长度
            System.out.println("对应的访问路径为->" + bestTour.toString());// 输出最优路径
        }
    }
}
