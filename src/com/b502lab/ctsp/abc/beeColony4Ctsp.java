package com.b502lab.ctsp.abc;

import com.b502lab.tsp.abc.beeColony;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.b502lab.ctsp.common.Ctsp.me;
import static com.b502lab.tsp.common.Constant.RANDOM_GEN;

public class beeColony4Ctsp extends beeColony {

    int[][] Foods = new int[FoodNumber][D];
    int[][] Salesmans = new int[FoodNumber][D];
    int[] solution = new int[D];
    int[] GlobalParams = new int[D];
    int[] GlobalSalesman = new int[D];

    public beeColony4Ctsp() {
        super(me.n - 1);// 0 is deport, auto add to the start and end position
    }

    @Override
    protected void init(int index) {
        List<Integer> nodesToVisit = new ArrayList<>(D);
        for (int i = 1; i <= D; i++) {// 0 is depot, we have to add it to the start and end position of every salesman's tour
            nodesToVisit.add(i);
        }
        Collections.shuffle(nodesToVisit);

        int[][] tours = new int[me.salesmen_count + 1][D];// record every salesman's tour
        for (int i = 0; i < nodesToVisit.size(); i++) {
            int curNode = nodesToVisit.get(i);
            int color = me.colors[curNode];
            if (color == 0) {
                addCurNode(tours, curNode);
            } else {
                // add cur node to the min position
                addCurNode(tours[color], curNode);
            }
        }
        // combine tour
        combineTours(tours, index);

        f[index] = calculateFunction(index);
        fitness[index] = CalculateFitness(f[index]);
        trial[index] = 0;
    }

    private void addCurNode(int[][] tours, int curNode) {
        int color = 1;
        int[] min = new int[]{0, Integer.MAX_VALUE};
        // find the min position and color
        for (int j = 1; j <= me.salesmen_count; j++) {
            int[] t = minPosition(curNode, tours[j]);
            if (t[1] < min[1]) {
                min[0] = t[0];
                min[1] = t[1];
                color = j;
            }
        }
        // insert into salesman color's tour
        insert(tours[color], min[0], curNode);
    }

    private void addCurNode(int[] tour, int curNode) {
        // find the min position
        int[] min = minPosition(curNode, tour);
        // insert into tour
        insert(tour, min[0], curNode);
    }

    private void insert(int[] tour, int index, int value) {
        // move the node
        for (int j = D - 1; j > index; j--) {
            tour[j] = tour[j - 1];
        }
        // insert cur_node
        tour[index] = value;
    }

    // curNode放到已有路径tour的何处，使得整个tour的路径长度增加最小
    // @return minPosition and minGain
    private int[] minPosition(int curNode, int[] tour) {
        if (tour[0] == 0) return new int[]{0, 0};
        int minPos = 0;
        int min = me.distance[0][curNode] + me.distance[curNode][tour[0]] - me.distance[0][tour[0]];
        for (int j = 1; j < D && tour[j] != 0; j++) {
            int t = me.distance[tour[j - 1]][curNode] + me.distance[curNode][tour[j]] - me.distance[tour[j - 1]][tour[j]];
            if (t < min) {
                min = t;
                minPos = j;
            }
        }
        return new int[]{minPos, min};
    }

    private void combineTours(int[][] tours, int index) {
        int k = 0;
        for (int color = 1; color <= me.salesmen_count; color++) {
            for (int j = 0; j < D && tours[color][j] != 0; j++) {
                Salesmans[index][k] = color;
                Foods[index][k++] = tours[color][j];
            }
        }
    }

    /*The best food source is memorized*/
    protected void MemorizeBestSource() {
        int i, j;

        bestUnchanged = true;
        for (i = 0; i < FoodNumber; i++) {
            if (f[i] < GlobalMin) {
                GlobalMin = f[i];
                for (j = 0; j < D; j++) {
                    GlobalParams[j] = Foods[i][j];
                    GlobalSalesman[j] = Salesmans[i][j];
                }
                bestUnchanged = false;
            }
        }
    }

    double getPcp() {
        double mincp = 0.2;
        double maxcp = 0.9;
        return (maxcp - mincp) * iter / maxCycle + mincp;
    }

    void neighbouringSolutionGeneration(int index) {
        double Pcp = getPcp();

        // INTRA method
        int[][] tours = new int[me.salesmen_count + 1][D];// record every salesman's tour
        List<Integer>[] unassignedCities = new ArrayList[me.salesmen_count + 1];
        for (int i = 0; i < unassignedCities.length; i++) {
            unassignedCities[i] = new ArrayList<>();
        }
        for (int color = 1; color <= me.salesmen_count; color++) {
            for (int i = 0, k = 0; i < Foods[index].length; i++) {
                if (color == Salesmans[index][i]) {
                    double r = RANDOM_GEN.nextDouble();
                    if (r < Pcp) {
                        tours[color][k++] = Foods[index][i];
                    } else {
                        unassignedCities[color].add(Foods[index][i]);
                    }
                }
            }
        }

        for (int color = 1; color <= me.salesmen_count; color++) {
            Collections.shuffle(unassignedCities[color]);
            for (int curNode : unassignedCities[color]) {
                addCurNode(tours[color], curNode);
            }
        }
        // set the tours as the index Food
        combineTours(tours, index);

        // INTER method
        tours = new int[me.salesmen_count + 1][D];// record every salesman's tour
        unassignedCities = new ArrayList[me.salesmen_count + 1];
        for (int i = 0; i < unassignedCities.length; i++) {
            unassignedCities[i] = new ArrayList<>();
        }

        for (int color = 1; color <= me.salesmen_count; color++) {
            for (int i = 0, k = 0; i < Foods[index].length; i++) {
                if (color == Salesmans[index][i]) {
                    double r = RANDOM_GEN.nextDouble();
                    if (r < Pcp) {
                        tours[color][k++] = Foods[index][i];
                    } else {
                        unassignedCities[color].add(Foods[index][i]);
                    }
                }
            }
        }

        for (int color = 1; color <= me.salesmen_count; color++) {
            Collections.shuffle(unassignedCities[color]);
            for (int curNode : unassignedCities[color]) {
                if (me.colors[curNode] == 0) {
                    addCurNode(tours, curNode);
                } else {
                    // add cur node to the min position
                    addCurNode(tours[color], curNode);
                }
            }
        }

        // set the tours as the index Food
        combineTours(tours, index);
    }

    @Override
    protected void SendEmployedBees() {
        int i, j;
        int[] old_food = new int[D];
        int[] old_salesman = new int[D];
      /*Employed Bee Phase*/
        for (i = 0; i < FoodNumber; i++) {
            for (j = 0; j < D; j++) {
                old_food[j] = Foods[i][j];
                old_salesman[j] = Salesmans[i][j];
            }

            // neighbour search
            neighbouringSolutionGeneration(i);

            ObjValSol = calculateFunction(i);
            FitnessSol = CalculateFitness(ObjValSol);

	        /*a greedy selection is applied between the current solution i and its mutant*/
            if (FitnessSol > fitness[i]) {

	        /*If the mutant solution is better than the current solution i, replace the solution with the mutant and reset the trial counter of solution i*/
                trial[i] = 0;
                f[i] = ObjValSol;
                fitness[i] = FitnessSol;
            } else {   /*if the solution i can not be improved, increase its trial counter*/
                trial[i] = trial[i] + 1;
                for (j = 0; j < D; j++) {// recovery the old solution
                    Foods[i][j] = old_food[j];
                    Salesmans[i][j] = old_salesman[j];
                }
            }


        }

	        /*end of employed bee phase*/

    }

    @Override
    protected void SendOnlookerBees() {

        int i, j, t;
        i = 0;
        t = 0;
        int[] old_food = new int[D];
        int[] old_salesman = new int[D];
      /*onlooker Bee Phase*/
        while (t < FoodNumber) {

            r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
            if (r < prob[i]) /*choose a food source depending on its probability to be chosen*/ {
                t++;

                for (j = 0; j < D; j++) {
                    old_food[j] = Foods[i][j];
                    old_salesman[j] = Salesmans[i][j];
                }

                ObjValSol = calculateFunction(i);
                FitnessSol = CalculateFitness(ObjValSol);

	        /*a greedy selection is applied between the current solution i and its mutant*/
                if (FitnessSol > fitness[i]) {
	        /*If the mutant solution is better than the current solution i, replace the solution with the mutant and reset the trial counter of solution i*/
                    trial[i] = 0;
                    f[i] = ObjValSol;
                    fitness[i] = FitnessSol;
                } else {   /*if the solution i can not be improved, increase its trial counter*/
                    trial[i] = trial[i] + 1;
                    for (j = 0; j < D; j++) {// recovery the old solution
                        Foods[i][j] = old_food[j];
                        Salesmans[i][j] = old_salesman[j];
                    }
                }
            } /*if */
            i++;
            if (i == FoodNumber)
                i = 0;
        }/*while*/

	        /*end of onlooker bee phase     */
    }

    @Override
    protected void SendScoutBees() {
        int maxtrialindex, i;
        maxtrialindex = 0;
        for (i = 1; i < FoodNumber; i++) {
            if (trial[i] > trial[maxtrialindex])
                maxtrialindex = i;
        }
        if (trial[maxtrialindex] >= limit) {
            init(maxtrialindex);
        }
    }

    int calculateFunction(int index) {
        int sum = 0;
        for (int color = 1; color <= me.salesmen_count; color++) {
            int pre = 0;
            for (int j = 0; j < D; j++) {
                if (Salesmans[index][j] == color) {
                    sum += me.distance[pre][Foods[index][j]];
                    pre = Foods[index][j];
                }
            }
            sum += me.distance[pre][0];
        }
        return sum;
    }

    @Override
    public double getBestValue() {
        return GlobalMin;
    }

    @Override
    public List<Integer> getBestTour() {
        List<Integer> tour = new ArrayList<>(D + me.salesmen_count + 1);
        for (int color = 1; color <= me.salesmen_count; color++) {
            tour.add(0);
            for (int i = 0; i < D; i++) {
                if (GlobalSalesman[i] == color) {
                    tour.add(GlobalParams[i]);
                }
            }
        }
        tour.add(0);

        return tour;
    }

}
