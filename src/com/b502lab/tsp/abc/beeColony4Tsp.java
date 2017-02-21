package com.b502lab.tsp.abc;

import com.b502lab.tsp.common.LocalSearch;
import com.b502lab.tsp.common.Tsp;

import java.util.*;

public class beeColony4Tsp extends beeColony {

    int[][] Foods = new int[FoodNumber][D];
    int[] solution = new int[D];
    int[] GlobalParams = new int[D];

    public beeColony4Tsp() {
        super(Tsp.me.n);
    }

    @Override
    protected void init(int index) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < D; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        for (int i = 0; i < D; i++) {
            Foods[index][i] = list.get(i);
        }

        f[index] = calculateFunction(Foods[index]);
        fitness[index] = CalculateFitness(f[index]);
        trial[index] = 0;
    }

    @Override
    protected void MemorizeBestSource() {
        int i, j;

        for (i = 0; i < FoodNumber; i++) {
            if (f[i] < GlobalMin) {
                GlobalMin = f[i];
                for (j = 0; j < D; j++)
                    GlobalParams[j] = Foods[i][j];
            }
        }
    }

    @Override
    protected void SendEmployedBees() {
        int i, j;
        int[] solution = new int[D + 1];
      /*Employed Bee Phase*/
        for (i = 0; i < FoodNumber; i++) {
            /*The parameter to be changed is determined randomly*/
            r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
            param2change = (int) (r * D);

	        /*A randomly chosen solution is used in producing a mutant solution of the solution i*/
            r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
            neighbour = (int) (r * FoodNumber);

	        /*Randomly selected solution must be different from the solution i*/
            // while(neighbour==i)
            // {
            // r = (   (double)Math.random()*32767 / ((double)(32767)+(double)(1)) );
            // neighbour=(int)(r*FoodNumber);
            // }
            for (j = 0; j < D; j++)
                solution[j] = Foods[i][j];
            solution[D] = solution[0];

	        /*v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
            r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
            // how to update
//            solution[param2change] = Foods[i][param2change] + (Foods[i][param2change] - Foods[neighbour][param2change]) * (r - 0.5) * 2;
            // 2-opt move
            LocalSearch.two_opt_first(solution);

	        /*if generated parameter value is out of boundaries, it is shifted onto the boundaries*/
//            if (solution[param2change] < lb)
//                solution[param2change] = lb;
//            if (solution[param2change] > ub)
//                solution[param2change] = ub;

            ObjValSol = calculateFunction(solution);
            FitnessSol = CalculateFitness(ObjValSol);

	        /*a greedy selection is applied between the current solution i and its mutant*/
            if (FitnessSol > fitness[i]) {

	        /*If the mutant solution is better than the current solution i, replace the solution with the mutant and reset the trial counter of solution i*/
                trial[i] = 0;
                for (j = 0; j < D; j++)
                    Foods[i][j] = solution[j];
                f[i] = ObjValSol;
                fitness[i] = FitnessSol;
            } else {   /*if the solution i can not be improved, increase its trial counter*/
                trial[i] = trial[i] + 1;
            }


        }

	        /*end of employed bee phase*/

    }

    @Override
    protected void SendOnlookerBees() {
        int i, t;
        i = 0;
        t = 0;
      /*onlooker Bee Phase*/
        while (t < FoodNumber) {

            r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
            if (r < prob[i]) /*choose a food source depending on its probability to be chosen*/ {
                t++;

	        /*The parameter to be changed is determined randomly*/
                r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
                param2change = (int) (r * D);

	        /*A randomly chosen solution is used in producing a mutant solution of the solution i*/
                r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
                neighbour = (int) (r * FoodNumber);

	        /*Randomly selected solution must be different from the solution i*/
                while (neighbour == i) {
                    //System.out.println(Math.random()*32767+"  "+32767);
                    r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
                    neighbour = (int) (r * FoodNumber);
                }
                for (int j = 0; j < D; j++)
                    solution[j] = Foods[i][j];

	        /*v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
//                r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
//                solution[param2change] = Foods[i][param2change] + (Foods[i][param2change] - Foods[neighbour][param2change]) * (r - 0.5) * 2;
                // learn with neighbour
                learnWith(neighbour);

	        /*if generated parameter value is out of boundaries, it is shifted onto the boundaries*/
//                if (solution[param2change] < lb)
//                    solution[param2change] = lb;
//                if (solution[param2change] > ub)
//                    solution[param2change] = ub;
                ObjValSol = calculateFunction(solution);
                FitnessSol = CalculateFitness(ObjValSol);

	        /*a greedy selection is applied between the current solution i and its mutant*/
                if (FitnessSol > fitness[i]) {
            /*If the mutant solution is better than the current solution i, replace the solution with the mutant and reset the trial counter of solution i*/
                    trial[i] = 0;
                    for (int j = 0; j < D; j++)
                        Foods[i][j] = solution[j];
                    f[i] = ObjValSol;
                    fitness[i] = FitnessSol;
                } else {   /*if the solution i can not be improved, increase its trial counter*/
                    trial[i] = trial[i] + 1;
                }
            } /*if */
            i++;
            if (i == FoodNumber)
                i = 0;
        }/*while*/

	        /*end of onlooker bee phase     */
    }

    // 学习操作：将于邻居不同的边重新散列，增加相似度
    private void learnWith(int neighbour) {
        int[] pos1 = new int[D];
        int[] pos2 = new int[D];
        for (int j = 0; j < D; j++) {
            pos1[solution[j]] = j;
            pos2[Foods[neighbour][j]] = j;
        }
        Set<Integer> sameVertex = new HashSet<>(D);
        for (int j = 0; j < D; j++) {
            if ((solution[(pos1[j] + 1) % D]) == (Foods[neighbour][(pos2[j] + 1) % D])) {
                sameVertex.add(j);
                sameVertex.add(solution[(pos1[j] + 1) % D]);
            }
        }
        // 将没有重复的点重新排列
        List<Integer> notSameVertex = new ArrayList<>(D);
        for (int j = 0; j < D; j++) {
            if (!sameVertex.contains(j))
                notSameVertex.add(j);
        }
        Collections.shuffle(notSameVertex);
        for (int j = 0, k = 0; j < notSameVertex.size(); j++) {
            for (; k < D; k++) {
                if (notSameVertex.contains(solution[k])) {
                    solution[k++] = notSameVertex.get(j);
                    break;
                }
            }
        }
    }

    @Override
    protected void SendScoutBees() {
        int maxtrialindex = 0;
        for (int i = 1; i < FoodNumber; i++) {
            if (trial[i] > trial[maxtrialindex])
                maxtrialindex = i;
        }
        // method 1: re-initial the food
//        if (trial[maxtrialindex] >= limit) {
//            init(maxtrialindex);
//        }

        // method 2: away from the neighbour
        r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
        neighbour = (int) (r * FoodNumber);

        for (int j = 0; j < D; j++)
            solution[j] = Foods[maxtrialindex][j];
        awayFrom(neighbour);

        for (int j = 0; j < D; j++)
            Foods[maxtrialindex][j] = solution[j];
        f[maxtrialindex] = calculateFunction(solution);
        fitness[maxtrialindex] = CalculateFitness(f[maxtrialindex]);
        trial[maxtrialindex] = 0;
    }

    // 侦察蜂可以这样得到一个新解
    private void awayFrom(int neighbour) {
        int[] pos1 = new int[D];
        int[] pos2 = new int[D];
        for (int j = 0; j < D; j++) {
            pos1[solution[j]] = j;
            pos2[Foods[neighbour][j]] = j;
        }
        Set<Integer> sameVertex = new HashSet<>(D);
        for (int j = 0; j < D; j++) {
            if ((solution[(pos1[j] + 1) % D]) == (Foods[neighbour][(pos2[j] + 1) % D])) {
                sameVertex.add(j);
                sameVertex.add(solution[(pos1[j] + 1) % D]);
            }
        }
        // 将相同的点重新排列
        List<Integer> sameVertexList = new ArrayList<>(sameVertex.size());
        for (int a : sameVertex)
            sameVertexList.add(a);
        Collections.shuffle(sameVertexList);
        for (int j = 0, k = 0; j < sameVertexList.size(); j++) {
            for (; k < D; k++) {
                if (sameVertexList.contains(solution[k])) {
                    solution[k++] = sameVertexList.get(j);
                    break;
                }
            }
        }
    }

    double calculateFunction(int[] sol) {
        int value = 0;
        value += Tsp.me.distance[sol[0]][sol[D - 1]];
        for (int i = 1; i < D; i++) {
            value += Tsp.me.distance[sol[i]][sol[i - 1]];
        }
        return value;
    }
}
