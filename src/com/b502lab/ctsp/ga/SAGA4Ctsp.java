package com.b502lab.ctsp.ga;

import java.util.Collections;
import java.util.List;

import static com.b502lab.ctsp.common.Ctsp.me;
import static com.b502lab.tsp.common.Constant.ALL_CTSP_PATH;
import static com.b502lab.tsp.common.Constant.RANDOM_GEN;

/**
 * Created by lan_cyl on 2017/1/6.
 */
public class SAGA4Ctsp extends HCGA4Ctsp {

    public SAGA4Ctsp(int n, int[][] dis) {
        super(n, dis);
    }

    @Override
    protected void localSearch() {
        values[currentBestPosition] = simulatedAnnealing(population[currentBestPosition], salesmanChromosome[currentBestPosition], values[currentBestPosition]);
        if (values[currentBestPosition] < bestValue) {
            best = cloneList(population[currentBestPosition]);
            bestSalesmanChromosome = cloneList(salesmanChromosome[currentBestPosition]);
            bestValue = values[currentBestPosition];
        }
    }

    /**
     * 产生邻居的方法：交换、置换、移位：http://wenku.baidu.com/link?url=ZFmG0O1s_Rzq0nyPuU8WfHupBsa7e3dDAkk3QUlQ3rcYYbCmz-CSuZYVWrBTmAMxJ-D7Ym6-Hi6WoroZa_qZgQz9KWRSu4cGycVUQEjKctq
     */
    public int simulatedAnnealing(List<Integer> individual, List<Integer> salesman, int value) {
        // 爬山失败，模拟退火接收部分较差移动
        int t0 = 100, coolingTimes = 60, stepLen = 30;
        double alfa = 0.9;
        double t = t0;
        for (int i = 0; i < coolingTimes; i++) {
            for (int j = 0; j < stepLen; j++) {
                int color = RANDOM_GEN.nextInt(me.salesmen_count) + 1;
                int[] rst = randomSwapTwoCity(individual, salesman, color);
                if (rst[0] < 0) {
                    value += rst[0];
                } else if (Math.exp((-rst[0]) / (t + 0.001)) > Math.random()) {
                    value += rst[0];
                } else {
                    Collections.swap(individual, rst[1], rst[2]);
                }
            }
            t = t * alfa;
        }
        return evaluate(individual, salesman);
    }

    public static void main(String[] args) {
        me.init(ALL_CTSP_PATH + "eil51-4.ctsp");
        SAGA4Ctsp GAG = new SAGA4Ctsp(me.n, me.distance);
        GAG.start();
        System.out.println(GAG.bestValue + ":" + GAG.best);
    }
}