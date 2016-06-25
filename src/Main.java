import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.b502lab.ctsp.ga.Ga4Ctsp;
import com.b502lab.ctsp.ga.GaG4Ctsp;
import com.b502lab.ctsp.ga.HcGa4Ctsp;
import com.b502lab.ctsp.ga.SaGa4Ctsp;
import com.b502lab.ctsp.ito.Ito4Ctsp;
import com.b502lab.ctsp.Method4Ctsp;
import com.b502lab.ctsp.common.ResultWriter;

public class Main {
	/** 测试算例文件夹 */
	private final static String rootPath = "dataset/";

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
		// List<File> filesList = initFilePath(new String[] { "eil51-4" });
		// List<File> filesList = initFilePath(new String[] { "gr229-10" });

		// String methodName = "Ito4Ctsp";
		// String methodName = "Ga4Ctsp";
		String methodName = args[1];

		Method4Ctsp method;
		switch (methodName) {
		case "Ito4Ctsp":
			method = new Ito4Ctsp();
			break;

		case "Ga4Ctsp":
			method = new Ga4Ctsp(false, false, false);
			break;
		case "GaG4Ctsp":
			method = new Ga4Ctsp(true, false, false);
			break;

		case "HcGa4Ctsp":
			method = new Ga4Ctsp(true, true, false);
			break;

		case "SaGa4Ctsp":
			method = new Ga4Ctsp(true, false, true);
			break;

		default:
			method = new Ito4Ctsp();
			break;
		}

		print(method, filesList, methodName);
	}

	private static void print(Method4Ctsp method, List<File> filesList, String methodName) {

		int count = 10;// 每个算例求解次数
		for (File inputFile : filesList) {
			double bestFitness = 1e10, worstFitness = 0;
			List<Integer> bestTour = null;
			double sumFit = 0;
			long startTime = System.nanoTime();

			/*------------算法执行count次，求平均值--------------*/
			for (int i = 0; i < count; i++) {
				method.execute(inputFile);// 算法执行
				sumFit += method.getBestFitness();
				if (bestFitness > method.getBestFitness()) {// 更新count次算法运行后的最好解
					bestFitness = method.getBestFitness();
					bestTour = method.getBestTour();
				}
				if (worstFitness < method.getBestFitness()) {// 更新count次算法运行后的最好解
					worstFitness = method.getBestFitness();
				}
			}
			long estimatedTime = System.nanoTime() - startTime;
			ResultWriter.write(
					rootPath + "result/" + method.getProblem().INSTANCE_NAME + "." + methodName + ".opt.tour",
					method.getProblem(), bestTour, bestFitness, worstFitness, sumFit / count,
					estimatedTime / (count * 1e9), true);
			/*------输出最优解--------*/
			System.out.println("平均解为->" + sumFit / count);// 输出平均路径长度
			System.out.println("最优解为->" + bestFitness);// 输出最优路径长度
			System.out.println("对应的访问路径为->" + bestTour.toString());// 输出最优路径
			System.out.println(bestTour.size());
		}
	}
}
