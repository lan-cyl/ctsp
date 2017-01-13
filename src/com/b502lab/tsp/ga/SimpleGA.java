package com.b502lab.tsp.ga;

import com.b502lab.tsp.common.Tsp;

import java.util.*;

import static com.b502lab.tsp.common.Constant.*;

/**
 * Created by lan_cyl on 2017/1/6.
 */
public class SimpleGA {

    private int UNCHANGED_GENS = 0;
    private int currentGeneration = 0;
    protected int mutationTimes = 0;

    protected int[] values;// 长度
    protected double[] fitnessValues;// 适应度
    protected double[] roulette;
    protected List<Integer>[] population;

    protected int n;
    protected int[][] dis;

    public List<Integer> best;
    public int bestValue = Integer.MAX_VALUE;
    protected int currentBestPosition;// 本轮迭代最优解是第几个

    public SimpleGA(int n, int[][] dis) {
        this.n = n;
        this.dis = dis;
    }

    protected void init() {
        values = new int[POPULATION_SIZE];
        fitnessValues = new double[POPULATION_SIZE];
        roulette = new double[POPULATION_SIZE];
        population = new ArrayList[POPULATION_SIZE];

        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = randomIndivial();
        }
        setBestValue();
    }

    public void start() {
        init();
        while (currentGeneration++ < 25000) {
            nextEpoch();
        }
    }

    protected void nextEpoch() {
        selection();
        crossover();
        mutation();

        //if(UNCHANGED_GENS > POPULATION_SIZE + ~~(points.length/10)) {
        //MUTATION_PROBABILITY = 0.05;
        //if(doPreciseMutate) {
        //  best = preciseMutate(best);
        //  best = preciseMutate1(best);
        //  if(evaluate(best) < bestValue) {
        //    bestValue = evaluate(best);
        //    UNCHANGED_GENS = 0;
        //    doPreciseMutate = true;
        //  } else {
        //    doPreciseMutate = false;
        //  }
        //}
        //} else {
        //doPreciseMutate = 1;
        //MUTATION_PROBABILITY = 0.01;
        //}
        setBestValue();
    }

    protected void selection() {
        List[] parents = new ArrayList[POPULATION_SIZE];

        setRoulette();
        for (int i = 0; i < POPULATION_SIZE; i++)
            parents[i] = cloneList(population[wheelOut(RANDOM_GEN.nextDouble())]);

        population = parents;
    }

    void crossover() {
        List<Integer> queue = new ArrayList();
        for (int i = 1; i < POPULATION_SIZE; i++) {
            if (Math.random() < CROSSOVER_PROBABILITY) {
                queue.add(i);
            }
        }
        Collections.shuffle(queue);

        // do crossover
        for (int i = 0, j = queue.size() - 1; i < j; i += 2) {
            doCrossover(queue.get(i), queue.get(i + 1));
        }
    }

    protected void doCrossover(int x, int y) {
        List child1 = getChild(x, y);
        List child2 = getChild(y, x);
        population[x] = child1;
        population[y] = child2;
    }

    List getChild(int x, int y) {
        List solution = new ArrayList();
        List px = cloneList(population[x]);
        List py = cloneList(population[y]);
        int dx, dy;
        int c = (int) px.get(RANDOM_GEN.nextInt(px.size()));
        solution.add(c);
        while (px.size() > 1) {
            int xi = px.indexOf(c);
            int yi = py.indexOf(c);
            xi = (xi + 1) % px.size();
            yi = (yi + 1) % py.size();
            dx = (int) px.get(xi);
            dy = (int) py.get(yi);
            px.remove((Object) c);
            py.remove((Object) c);
            c = dis[c][dx] < dis[c][dy] ? dx : dy;
            solution.add(c);
        }
        return solution;
    }

    void mutation() {
//        for (int i = 0; i < POPULATION_SIZE; i++) {
//            if (Math.random() < MUTATION_PROBABILITY) {
//                if (Math.random() > 0.5) {
//                    pushMutate(new_population[i]);
//                } else {
//                    doMutate(new_population[i]);
//                }
//                i--;
//            }
//        }
        for (int i = 0; i < POPULATION_SIZE; i++) {
            if (RANDOM_GEN.nextDouble() < MUTATION_PROBABILITY) {
                doMutate(i);
            }
        }
    }

    protected void doMutate(int i) {
        int m = RANDOM_GEN.nextInt(population[i].size());
        int n = RANDOM_GEN.nextInt(population[i].size());
        Collections.swap(population[i], m, n);
    }

    protected void setBestValue() {
        int currentBestValue = Integer.MAX_VALUE;
        for (int i = 0; i < population.length; i++) {
            values[i] = evaluate(i);
            if (values[i] < currentBestValue) {
                currentBestValue = values[i];
                currentBestPosition = i;
            }
        }
        if (bestValue > currentBestValue) {
            best = cloneList(population[currentBestPosition]);
            bestValue = currentBestValue;
            UNCHANGED_GENS = 0;
        } else {
            UNCHANGED_GENS += 1;
        }
    }

    protected void setRoulette() {
        //calculate all the fitness
        for (int i = 0; i < values.length; i++) {
            fitnessValues[i] = 1.0 / values[i];
        }
        //set the roulette
        double sum = 0;
        for (int i = 0; i < fitnessValues.length; i++) {
            sum += fitnessValues[i];
        }
        for (int i = 0; i < roulette.length; i++) {
            roulette[i] = fitnessValues[i] / sum;
        }
        for (int i = 1; i < roulette.length; i++) {
            roulette[i] += roulette[i - 1];
        }
    }

    protected int wheelOut(double rand) {
        int i;
        for (i = 0; i < roulette.length; i++) {
            if (rand <= roulette[i]) {
                return i;
            }
        }
        return i;
    }

    protected List randomIndivial() {
        List a = new ArrayList();
        for (int i = 0; i < n; i++) {
            a.add(i);
        }
        Collections.shuffle(a);
        return a;
    }

    protected int evaluate(int a) {
        List<Integer> indivial = population[a];
        int sum = dis[indivial.get(0)][indivial.get(indivial.size() - 1)];
        for (int i = 1; i < indivial.size(); i++) {
            sum += dis[indivial.get(i)][indivial.get(i - 1)];
        }
        return sum;
    }

    protected List<Integer> cloneList(List<Integer> best) {
        return (List<Integer>) ((ArrayList) best).clone();
    }
}
