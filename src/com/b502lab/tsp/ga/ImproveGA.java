package com.b502lab.tsp.ga;

import com.b502lab.tsp.ga.SimpleGA;

import java.util.*;

import static com.b502lab.tsp.common.Constant.POPULATION_SIZE;
import static com.b502lab.tsp.common.Constant.RANDOM_GEN;

/**
 * Created by lan_cyl on 2017/1/6.
 */
public class ImproveGA extends SimpleGA {

    protected List[] old_population = new List[POPULATION_SIZE];
    protected int[] old_values = new int[POPULATION_SIZE];

    public ImproveGA(int n, int[][] dis) {
        super(n, dis);
    }

    protected void selection() {

        for (int i = 0; i < population.length; i++) {
            old_population[i] = cloneList(population[i]);
            old_values[i] = values[i];
        }

        // elite selection
//        eliteSelection();

        // roulette wheel and elite selection
        rouletteAndElitSelection();
    }

    private void rouletteAndElitSelection() {
        List<Integer>[] new_population = new List[POPULATION_SIZE * 2];
        int[] new_values = new int[POPULATION_SIZE * 2];
        for (int i = 0; i < population.length; i++) {
            new_population[i] = population[i];
            new_population[population.length + i] = old_population[i];
            new_values[i] = values[i];
            new_values[population.length + i] = old_values[i];
        }
        population = new_population;
        values = new_values;
        fitnessValues = new double[values.length];
        roulette = new double[values.length];

        super.selection();
        // select four elite
        population[0] = cloneList(population[currentBestPosition]);
        population[1] = cloneList(best);
        population[2] = doMutate(cloneList(best));
        population[3] = pushMutate(cloneList(best));
    }

    void eliteSelection() {
        int[] old_sortedIndex = sort(old_values);
        int[] sortedIndex = sort(values);

        mergeElite(old_sortedIndex, sortedIndex);
    }

    void mergeElite(int[] old_sortedIndex, int[] sortedIndex) {
        List<Integer>[] new_population = new ArrayList[POPULATION_SIZE];
        int[] new_value = new int[POPULATION_SIZE];
        for (int i = 0, j = 0, k = 0; k < POPULATION_SIZE; k++) {
            if (k > 0) {
                while (i < old_sortedIndex.length && old_values[old_sortedIndex[i]] == new_value[k - 1]) i++;
                while (j < sortedIndex.length && values[sortedIndex[j]] == new_value[k - 1]) j++;
            }
            if (i >= old_sortedIndex.length && j >= sortedIndex.length)
                new_population[k] = doMutate(cloneList(best));
            else if (j >= sortedIndex.length || i < old_sortedIndex.length && old_values[old_sortedIndex[i]] < values[sortedIndex[j]]) {
                new_population[k] = old_population[old_sortedIndex[i]];
                new_value[k] = old_values[old_sortedIndex[i]];
                i++;
            } else {
                new_population[k] = population[sortedIndex[j]];
                new_value[k] = values[sortedIndex[j]];
                j++;
            }
        }
        population = new_population;
    }

    // sort population by fitness
    protected int[] sort(int[] values2) {
        int[] sortedIndex = new int[values2.length];
        int[] values = values2.clone();
        for (int i = 0; i < values.length; i++) {
            int k = i;
            for (int j = i + 1; j < values.length; j++) {
                if (values[j] < values[k])
                    k = j;
            }
            sortedIndex[i] = k;
            if (k != i) {// swap k,i
                int t = values[i];
                values[i] = values[k];
                values[k] = t;
            }
        }
        return sortedIndex;
    }

    // 对最优解进行大胆变异的两种方式
    protected List<Integer> doMutate(List<Integer> seq) {
        mutationTimes++;
        // m and n refers to the actual index in the array
        // m range from 0 to length-2, n range from 2...length-m
        int m, n;
        do {
            m = RANDOM_GEN.nextInt(seq.size() - 2);
            n = RANDOM_GEN.nextInt(seq.size());
        } while (m >= n);

        for (int i = 0, j = (n - m + 1) >> 1; m + i < n - j; i++) {
            Collections.swap(seq, m + i, n - i);
        }
        return seq;
    }

    protected List<Integer> pushMutate(List<Integer> seq) {
        mutationTimes++;
        int m, n;
        do {
            m = RANDOM_GEN.nextInt(seq.size() >> 1);
            n = RANDOM_GEN.nextInt(seq.size());
        } while (m >= n);

        List a = seq.subList(0, m);
        List b = seq.subList(m, n);
        List c = seq.subList(n, seq.size());
        List d = new ArrayList();
        d.addAll(b);
        d.addAll(a);
        d.addAll(c);
        return d;
    }

}
