package com.b502lab.ctsp.ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.b502lab.ctsp.common.Ctsp.me;
import static com.b502lab.tsp.common.Constant.ALL_CTSP_PATH;
import static com.b502lab.tsp.common.Constant.RANDOM_GEN;

/**
 * Created by lan_cyl on 2017/1/6.
 */
public class HCGA4Ctsp extends GAG4Ctsp {

    public HCGA4Ctsp(int n, int[][] dis) {
        super(n, dis);
    }

    @Override
    public void nextEpoch() {
        super.nextEpoch();
        localSearch();
    }

    protected void localSearch() {
        values[currentBestPosition] = hillClambing(population[currentBestPosition], salesmanChromosome[currentBestPosition], values[currentBestPosition]);
        if (values[currentBestPosition] < bestValue) {
            best = cloneList(population[currentBestPosition]);
            bestSalesmanChromosome = cloneList(salesmanChromosome[currentBestPosition]);
            bestValue = values[currentBestPosition];
        }
    }

    /**
     * 自己来爬个山：对于m个旅行商，每个旅行商随机从自己的路径中选出两个交换一下，如果效果好就替换，然后下一个旅行商再重复
     * 产生邻居的方法：交换、置换、移位：http://wenku.baidu.com/link?url=ZFmG0O1s_Rzq0nyPuU8WfHupBsa7e3dDAkk3QUlQ3rcYYbCmz-CSuZYVWrBTmAMxJ-D7Ym6-Hi6WoroZa_qZgQz9KWRSu4cGycVUQEjKctq
     */
    int hillClambing(List<Integer> individual, List<Integer> salesman, int value) {
        for (int color = 1; color <= me.salesmen_count; color++) {
            int[] rst = randomSwapTwoCity(individual, salesman, color);
            if (rst[0] >= 0)
                Collections.swap(individual, rst[1], rst[2]);
        }
        return evaluate(individual, salesman);
    }

    int kopt(List<Integer> individual, List<Integer> salesman, int color) {
        int gainvalue = 0;
        List<Integer> tourIndex = getTourIndex(salesman, color);
        for (int i = 0; i < tourIndex.size() - 1; i++) {
            int mini = 0, minj = 0, mingain = Integer.MAX_VALUE;
            for (int j = i + 1; j < tourIndex.size(); j++) {
                int gain = compuetGain(individual, tourIndex, i, j);
                if (gain < mingain) {
                    mingain = gain;
                    mini = tourIndex.get(i);
                    minj = tourIndex.get(j);
                }
            }
            if (mingain < 0) {
                Collections.swap(individual, mini, minj);
                gainvalue += mingain;
            }
        }
        return gainvalue;
    }

    int[] randomSwapTwoCity(List<Integer> individual, List<Integer> salesman, int color) {
        List<Integer> tourIndex = getTourIndex(salesman, color);
        int m, n;
        do {
            m = RANDOM_GEN.nextInt(tourIndex.size());
            n = RANDOM_GEN.nextInt(tourIndex.size());
        } while (m == n);
        if (m > n) {
            int t = m;
            m = n;
            n = t;
        }

        int[] rst = new int[3];
        rst[0] = compuetGain(individual, tourIndex, m, n);
        rst[1] = tourIndex.get(m);
        rst[2] = tourIndex.get(n);
        Collections.swap(individual, tourIndex.get(m), tourIndex.get(n));
        return rst;
    }

    int compuetGain(List<Integer> individual, List<Integer> tourIndex, int i, int j) {
        int pre = i == 0 ? 0 : individual.get(tourIndex.get(i - 1));
        int cur = individual.get(tourIndex.get(i));
        int next = individual.get(tourIndex.get(i + 1));

        int pre2 = individual.get(tourIndex.get(j - 1));
        int cur2 = individual.get(tourIndex.get(j));
        int next2 = j == tourIndex.size() - 1 ? 0 : individual.get(tourIndex.get(j + 1));
        int oldvalue = dis[pre][cur] + dis[cur][next] + dis[pre2][cur2] + dis[cur2][next2];
        int newvalue = dis[pre][cur2] + dis[cur2][next] + dis[pre2][cur] + dis[cur][next2];
        if (j == i + 1) {
            oldvalue = dis[pre][cur] + dis[cur][next] + dis[next][next2];
            newvalue = dis[pre][cur2] + dis[cur2][cur] + dis[cur][next2];
        }
        return newvalue - oldvalue;
    }

    List<Integer> getTourIndex(List<Integer> salesmanChrom, int color) {
        List<Integer> index = new ArrayList<>(salesmanChrom.size());
        for (int i = 0; i < salesmanChrom.size(); i++) {
            if (salesmanChrom.get(i) == color) {
                index.add(i);
            }
        }
        return index;
    }

}