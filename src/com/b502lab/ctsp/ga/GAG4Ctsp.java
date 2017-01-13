package com.b502lab.ctsp.ga;

import java.util.Collections;

import static com.b502lab.ctsp.common.Ctsp.me;
import static com.b502lab.tsp.common.Constant.ALL_CTSP_PATH;

/**
 * Created by lan_cyl on 2017/1/6.
 */
public class GAG4Ctsp extends GA4Ctsp {

    public GAG4Ctsp(int n, int[][] dis) {
        super(n, dis);
    }

    protected void init() {
        super.init();
        greedy();
        setBestValue();
    }

    private void greedy() {
        for (int i = 0; i < population.length; i++) {
            for (int color = 1; color <= me.salesmen_count; color++) {
                int pre = 0;
                for (int j = 0; j < population[i].size(); j++) {
                    if (salesmanChromosome[i].get(j) == color) {
                        int minIdx = j;
                        for (int k = j + 1; k < population[i].size(); k++) {
                            if (salesmanChromosome[i].get(k) == color && dis[population[i].get(k)][pre] < dis[population[i].get(minIdx)][pre]) {
                                minIdx = k;
                            }
                        }
                        Collections.swap(population[i], j, minIdx);
                        pre = population[i].get(j);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        me.init(ALL_CTSP_PATH + "eil51-4.ctsp");
        GAG4Ctsp GAG = new GAG4Ctsp(me.n, me.distance);
        GAG.start();
    }
}