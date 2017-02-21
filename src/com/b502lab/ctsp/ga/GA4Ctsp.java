package com.b502lab.ctsp.ga;

import com.b502lab.ctsp.common.Base;
import com.b502lab.tsp.ga.ImproveGA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.b502lab.ctsp.common.Ctsp.me;
import static com.b502lab.tsp.common.Constant.*;

/**
 * Created by lan_cyl on 2017/1/6.
 */
public class GA4Ctsp extends ImproveGA implements Base {

    protected List<Integer>[] salesmanChromosome;// 种群中每个个体的旅行商排列
    private List<Integer>[] old_salesmanChromosome;
    protected List<Integer> bestSalesmanChromosome;

    public GA4Ctsp(int n, int[][] dis) {
        super(n, dis);
    }

    public void init() {
        super.init();
        salesmanChromosome = new ArrayList[population.length];
        old_salesmanChromosome = new ArrayList[population.length];
        for (int i = 0; i < population.length; i++) {
            salesmanChromosome[i] = randomIndivialSalesmen(population[i]);
        }
        setBestValue();
    }

    @Override
    public void nextEpoch() {
        super.nextEpoch();
    }

    @Override
    protected void selection() {
        for (int i = 0; i < population.length; i++) {
            old_population[i] = cloneList(population[i]);
            old_salesmanChromosome[i] = cloneList(salesmanChromosome[i]);
            old_values[i] = values[i];
        }
        // elite selection
//        eliteSelection();

        // roulette wheel and elite selection
        rouletteAndElitSelection();
    }

    void eliteSelection() {
        int[] old_sortedIndex = sort(old_values);
        int[] sortedIndex = sort(values);

        merge(old_sortedIndex, sortedIndex);
    }

    void merge(int[] old_sortedIndex, int[] sortedIndex) {

        List[] new_population = new List[POPULATION_SIZE];
        List[] new_salesmanChrom = new List[POPULATION_SIZE];
        int elitenum = 3;

        new_population[0] = cloneList(best);
        new_salesmanChrom[0] = cloneList(bestSalesmanChromosome);

        new_population[1] = doMutate(cloneList(best));
        new_salesmanChrom[1] = cloneList(bestSalesmanChromosome);
        correctSalesman(new_population[1], new_salesmanChrom[1]);

        new_population[2] = pushMutate(cloneList(best));
        new_salesmanChrom[2] = cloneList(bestSalesmanChromosome);
        correctSalesman(new_population[2], new_salesmanChrom[2]);

        for (int i = 0, j = 0, k = elitenum; k < POPULATION_SIZE; k++) {
            if (k > 0) {
                while (i < old_sortedIndex.length && isSameList(old_population[old_sortedIndex[j]], new_population[k - 1]))
                    i++;
                while (j < sortedIndex.length && isSameList(population[sortedIndex[j]], new_population[k - 1])) j++;
            }
            if (i >= old_sortedIndex.length && j >= sortedIndex.length) {
                new_population[k] = doMutate(cloneList(best));
                new_salesmanChrom[k] = cloneList(bestSalesmanChromosome);
                correctSalesman(new_population[k], new_salesmanChrom[k]);
            } else if (j >= sortedIndex.length || i < old_sortedIndex.length && old_values[old_sortedIndex[i]] < values[sortedIndex[j]]) {
                new_population[k] = old_population[old_sortedIndex[i]];
                new_salesmanChrom[k] = old_salesmanChromosome[old_sortedIndex[i]];
                correctSalesman(new_population[k], new_salesmanChrom[k]);
                i++;
            } else {
                new_population[k] = population[sortedIndex[j]];
                new_salesmanChrom[k] = salesmanChromosome[sortedIndex[j]];
                correctSalesman(new_population[k], new_salesmanChrom[k]);
                j++;
            }
        }
        population = new_population;
        salesmanChromosome = new_salesmanChrom;
    }

    void rouletteAndElitSelection() {
        // copy all individual
        List<Integer>[] new_population = new List[POPULATION_SIZE * 2];
        List<Integer>[] new_salesmanChrom = new List[POPULATION_SIZE * 2];
        int[] new_values = new int[POPULATION_SIZE * 2];

        for (int i = 0; i < population.length; i++) {
            new_population[i] = population[i];
            new_population[population.length + i] = old_population[i];
            new_salesmanChrom[i] = salesmanChromosome[i];
            new_salesmanChrom[population.length + i] = old_salesmanChromosome[i];
            new_values[i] = values[i];
            new_values[population.length + i] = old_values[i];
        }
        population = new_population;
        salesmanChromosome = new_salesmanChrom;
        values = new_values;
        fitnessValues = new double[values.length];
        roulette = new double[values.length];

        // roulette wheel select
        new_population = new List[POPULATION_SIZE];
        new_salesmanChrom = new List[POPULATION_SIZE];
        int elitenum = 3;

        new_population[0] = cloneList(best);
        new_salesmanChrom[0] = cloneList(bestSalesmanChromosome);

        new_population[1] = doMutate(cloneList(best));
        new_salesmanChrom[1] = cloneList(bestSalesmanChromosome);
        correctSalesman(new_population[1], new_salesmanChrom[1]);

        new_population[2] = pushMutate(cloneList(best));
        new_salesmanChrom[2] = cloneList(bestSalesmanChromosome);
        correctSalesman(new_population[2], new_salesmanChrom[2]);

        setRoulette();
        for (int i = elitenum; i < POPULATION_SIZE; i++) {
            int k = wheelOut(RANDOM_GEN.nextDouble());
            new_population[i] = cloneList(population[k]);
            new_salesmanChrom[i] = cloneList(salesmanChromosome[k]);
        }

        population = new_population;
        salesmanChromosome = new_salesmanChrom;
    }

    @Override
    protected void doCrossover(int x, int y) {
        int m, n, len = me.n - 1;
        do {
            m = RANDOM_GEN.nextInt(len >> 1);
            n = RANDOM_GEN.nextInt(len);
        } while (m >= n);

        int[] x2y = new int[len + 1];
        int[] y2x = new int[len + 1];
        for (int i = m; i <= n; i++) {
            int a = population[x].get(i);
            int b = population[y].get(i);
            x2y[a] = b;
            y2x[b] = a;
            population[x].set(i, b);
            population[y].set(i, a);
//            int t = salesmanChromosome[x].get(i);
//            salesmanChromosome[x].set(i, salesmanChromosome[y].get(i));
//            salesmanChromosome[y].set(i, t);
        }
        for (int i = 0; i < len; i++) {
            // correct the duplicate city
            if (i < m || i > n)
                for (int j = m; j <= n; j++) {
                    if (population[x].get(i) == population[x].get(j)) {
                        int a = population[x].get(i);
                        while (y2x[a] != 0) a = y2x[a];
                        population[x].set(i, a);
                    }
                    if (population[y].get(i) == population[y].get(j)) {
                        int a = population[y].get(i);
                        while (x2y[a] != 0) a = x2y[a];
                        population[y].set(i, a);
                    }
                }
        }

        correctSalesman(population[x], salesmanChromosome[x]);
        correctSalesman(population[y], salesmanChromosome[y]);
    }

    // correct the corresponding salesman
    private void correctSalesman(List<Integer> individual, List salesman) {
        for (int i = 0; i < me.n - 1; i++) {
            // correct the corresponding salesman
            int colora = me.colors[individual.get(i)];
            if (colora != 0)
                salesman.set(i, colora);
        }
    }

    @Override
    protected void doMutate(int i) {
        int m = RANDOM_GEN.nextInt(population[i].size());
        int n = RANDOM_GEN.nextInt(population[i].size());
        Collections.swap(population[i], m, n);
        Collections.swap(salesmanChromosome[i], m, m);
    }

    // 随机生成初始解
    protected List randomIndivial() {
        List<Integer> a = new ArrayList();
        for (int i = 1; i < n; i++) {
            a.add(i);
        }
        Collections.shuffle(a);

        return a;
    }

    // 生成对应解的旅行商排列
    private List randomIndivialSalesmen(List<Integer> a) {
        List<Integer> b = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            int color = me.colors[a.get(i)];
            if (color == 0) {
                b.add(RANDOM_GEN.nextInt(me.salesmen_count) + 1);
            } else {
                b.add(color);
            }
        }
        return b;
    }

    @Override
    protected void setBestValue() {
        if (salesmanChromosome == null)
            return;
        super.setBestValue();
        if (bestValue == values[currentBestPosition]) {
            bestSalesmanChromosome = cloneList(salesmanChromosome[currentBestPosition]);
//            System.out.println(bestValue + ":" + bestSalesmanChromosome);
        }
    }

    int evaluate(List<Integer> indivial, List<Integer> salesman) {
        int sum = 0;
        for (int i = 1; i <= me.salesmen_count; i++) {
            int pre = 0;
            for (int j = 0; j < indivial.size(); j++) {
                if (salesman.get(j) == i) {
                    sum += dis[pre][indivial.get(j)];
                    pre = indivial.get(j);
                }
            }
            sum += dis[pre][0];
        }
        return sum;
    }

    @Override
    protected int evaluate(int a) {
        List<Integer> indivial = population[a];
        List<Integer> salesman = salesmanChromosome[a];
        return evaluate(indivial, salesman);
    }

    private boolean isSameList(List<Integer> a, List<Integer> b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i) != b.get(i))
                return false;
        }
        return true;
    }

    @Override
    public boolean bestUnchanged() {
        return bestUnchanged;
    }

    @Override
    public double getBestValue() {
        return bestValue;
    }

    @Override
    public List<Integer> getBestTour() {
        List<Integer> tour = new ArrayList<>();
        tour.add(0);
        for (int color = 1; color <= me.salesmen_count; color++) {
            for (int i = 0; i < bestSalesmanChromosome.size(); i++) {
                if (bestSalesmanChromosome.get(i) == color) {
                    tour.add(best.get(i));
                }
            }
            tour.add(0);
        }
        return tour;
    }
}