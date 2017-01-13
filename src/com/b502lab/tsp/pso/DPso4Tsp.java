package com.b502lab.tsp.pso;

import java.util.Random;

import static com.b502lab.tsp.common.Tsp.me;

public class DPso4Tsp {

    private final int popution = 50;// 粒子数
    private final int MaxSpeedLen = 30;// 速度长度限制
    private final double c1 = 2.8;
    private final double c2 = 1.3;
    private double r1;// 加速因子[0,1]之间的随机数
    private double r2;
    private double r3;
    private double w;// 惯性权值
    private double Max_W = 0.95;// 惯性权值
    private double Min_W = 0.4;// 惯性权值
    private final int MaxGenerion = 10000;// 迭代次数
    private final Random rand = new Random();

    class Particle {// 定义粒子类
        int[] tour;// 记录该粒子所选择的路径
        double fitness;// 适应度值
        int[] everBestTour;// 该粒子曾经得到的最优路径
        double everBestFitness;// 该粒子曾经得到的最优值
        int[] speed;// 记录该粒子的速度，即置换序列
    }

    private int generation;
    private Particle[] particles;// 粒子群
    private int[] untilBestTour;// 目前位置全局最优解
    private double untilBestFitness;

    public static void main(String[] args) {
        DPso4Tsp dpso = new DPso4Tsp();
        dpso.method();
    }

    private void method() {
        init();
        while (generation < MaxGenerion) {

            calFitness();
            findTheBest();
            update();
            generation++;
        }
        System.out.println("\n最好解为：" + untilBestFitness);
        System.out.println("路径为：");
        for (int i = 0; i < untilBestTour.length; i++) {
            System.out.print(untilBestTour[i] + "->");
        }
        // dispCurrBestAnswer();
    }

    private void dispCurrBestAnswer() {
        int[] t = {1, 22, 8, 26, 31, 28, 3, 36, 35, 20, 2, 29, 21, 16, 50, 34,
                30, 9, 49, 10, 39, 33, 45, 15, 44, 42, 40, 19, 41, 13, 25, 14,
                24, 43, 7, 23, 48, 6, 27, 51, 46, 12, 47, 18, 4, 17, 37, 5, 38,
                11, 32};
        System.out.println("\n已知最好解为：" + compute_tour_length(t));
        System.out.println("路径为：");
        for (int i = 0; i < t.length; i++) {
            System.out.print((t[i]) + "->");
        }
    }

    // 读取tsp文件，计算各节点之间的距离
    private void init() {
        generation = 0;
        particles = new Particle[popution];
        untilBestFitness = Double.MAX_VALUE;
        me.init("./ALL_tsp/eil51.impl");

        untilBestTour = new int[me.n];

        for (int i = 0; i < popution; i++) {// 贪心法初始化每一个粒子
            initParticle(i);
        }
    }

    // 初始化第i个粒子
    private void initParticle(int i) {
        particles[i] = new Particle();
        particles[i].tour = new int[me.n];
        particles[i].speed = new int[me.n];
        particles[i].everBestFitness = Double.MAX_VALUE;
        particles[i].everBestTour = new int[me.n];
        int[] tmp = new int[me.n];
        for (int k = 0; k < me.n; k++) {
            tmp[k] = k + 1;
        }
        int selectCity;
        for (int k = 0; k < me.n; k++) {// 完全随机得到初始路径
            selectCity = rand.nextInt(me.n);
            while (tmp[selectCity] == -1) {
                selectCity = (selectCity + 1) % me.n;
            }
            particles[i].tour[k] = tmp[selectCity];
            tmp[selectCity] = -1;
        }
        // 完全随机初始化速度
        for (int k = 0; k < me.n; k++) {
            particles[i].speed[k] = 0;
        }
    }

    /**
     * 计算适应度值
     */
    private void calFitness() {
        for (int i = 0; i < particles.length; i++) {
            particles[i].fitness = compute_tour_length(particles[i].tour);
        }
    }

    /**
     * 找到全局和局部最优
     */
    private void findTheBest() {
        for (int i = 0; i < particles.length; i++) {
            particles[i].fitness = compute_tour_length(particles[i].tour);
            if (particles[i].fitness < particles[i].everBestFitness) {
                particles[i].everBestFitness = particles[i].fitness;
                for (int j = 0; j < particles[i].tour.length; j++) {
                    particles[i].everBestTour[j] = particles[i].tour[j];
                }
            }
            if (particles[i].fitness < untilBestFitness) {
                untilBestFitness = particles[i].fitness;
                for (int j = 0; j < particles[i].tour.length; j++) {
                    untilBestTour[j] = particles[i].tour[j];
                }
            }
        }
    }

    /**
     * 更新粒子群
     */
    private void update() {
        w = Max_W - (Max_W - Min_W) * generation / MaxGenerion;
        r1 = rand.nextDouble();
        r2 = rand.nextDouble();
        r3 = rand.nextDouble();
        for (int i = 0; i < particles.length; i++) {// 更新每一个粒子
            // 更新速度V=c1(Xpbest-X)+c2(Xgbest-X)
            particles[i].speed = speedAdd(particles[i].speed,
                    mult(r1, sub(particles[i].everBestTour, particles[i].tour)));
            particles[i].speed = speedAdd(particles[i].speed,
                    mult(r2, sub(untilBestTour, particles[i].tour)));
            // 更新状态X=X+V
            particles[i].tour = tourAdd(particles[i].tour, particles[i].speed);
        }
    }

    // 位置与位置的减法结果得到一组置换序列
    private int[] sub(int[] tour1, int[] tour2) {
        int[] tmp = tour1.clone();
        for (int i = 0; i < tour1.length; i++) {
            if (tour1[i] == tour2[i]) {
                tmp[i] = 0;
            }
        }
        return tmp;
    }

    // 实数与速度的乘积操作，具有概率意义
    private int[] mult(double w, int[] speed) {
        int[] tmp = speed.clone();
        double r = rand.nextDouble();
        for (int i = 0; i < speed.length; i++) {
            if (r >= w) {
                tmp[i] = 0;
            }
            r = rand.nextDouble();
        }
        return tmp;
    }

    // 两个速度进行加法
    private int[] speedAdd(int[] speed1, int[] speed2) {
        int[] tmp = speed2.clone();
        for (int i = 0; i < speed2.length; i++) {
            if (tmp[i] == 0) {
                tmp[i] = speed1[i];
            }
        }
        return tmp;
    }

    // 位置与速度的加操作，得到置换后的序列
    private int[] tourAdd(int[] tour, int[] speed) {
        int[] tmp = tour.clone();
        for (int i = 0; i < tour.length; i++) {
            if (speed[i] != 0 && speed[i] != tour[i]) {
                int t = tmp[i];
                tmp[i] = speed[i];
                for (int j = 0; j < tour.length; j++) {
                    if (tmp[j] == speed[i]) {
                        tmp[j] = t;
                        break;
                    }
                }
            }
        }
        return tmp;
    }

    /**
     * FUNCTION: compute the tour length of tour t // 计算旅行 方案t的路程长度
     */
    double compute_tour_length(int[] t) {
        int i;
        double tour_length = 0;

        for (i = 0; i < t.length - 1; i++) {
            tour_length += me.distance[t[i] - 1][t[i + 1] - 1];
        }
        tour_length += me.distance[t[t.length - 1] - 1][t[0] - 1];
        return tour_length;
    }
}
