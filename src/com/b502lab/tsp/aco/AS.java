package com.b502lab.tsp.aco;

import com.b502lab.ctsp.common.Base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.b502lab.tsp.common.Constant.*;

public class AS implements Base {

    private boolean bestUnchanged;// 最优解是否更新

    protected List<Integer>[] m_nAnts;// 蚁群
    protected int[] values;
    protected int m_nIterCounter = 0;// 当前代数

    private int[][] m_distance;// 距离
    public double[][] m_tau;// 信息素浓度
    protected double[][] total;// 概率
    protected int m_nNodes;// 节点个数
    protected double m_dTau0;

    public List<Integer> so_far_best;
    public int so_far_best_value;
    public List<Integer> iter_best;
    public int iter_best_value;
    public int s_nLastBestPathIteration = 0;

    public AS(int n, int[][] distance) {
        m_nNodes = n;
        m_distance = distance;

    }

    @Override
    public void init() {

        m_nAnts = new List[nAnts];
        values = new int[nAnts];
        m_tau = new double[m_nNodes][m_nNodes];
        total = new double[m_nNodes][m_nNodes];
        so_far_best_value = Integer.MAX_VALUE;
        iter_best_value = Integer.MAX_VALUE;

        resetTau();
    }

    public void setTau(int r, int s, double value) {
        m_tau[r][s] = value;
        m_tau[s][r] = value;
        total[r][s] = m_tau[r][s] * Math.pow(1.0 / m_distance[r][s], B);
        total[s][r] = total[r][s];
    }

    protected void resetTau() {
        // calculate average distance
        double dSum = 0;
        for (int r = 0; r < m_nNodes; r++)
            for (int s = 0; s < m_nNodes; s++)
                dSum += m_distance[r][s];

        double dAverage = dSum / (double) (m_nNodes * m_nNodes);

        // set tau0
        m_dTau0 = 1 / (m_nNodes * (0.5 * dAverage));

        // initial tau matrix
        for (int r = 0; r < m_nNodes; r++) {
            for (int s = 0; s <= r; s++) {
                setTau(r, s, m_dTau0);
            }
        }
    }

    public int stateTransitionRule(int nCurNode, Set<Integer> nodesToVisit) {
        double dSum = 0;
        int nNextNode = -1;

        // get the sum at denominator
        for (int nNode : nodesToVisit) {
            if (m_tau[nCurNode][nNode] == 0)
                throw new RuntimeException("tau = 0");

            // Update the sum
            dSum += total[nCurNode][nNode];
        }

        if (dSum == 0)
            throw new RuntimeException("SUM = 0");

        double p = dSum * RANDOM_GEN.nextDouble();
        dSum = 0;

        // search the node in agreement with eq. b)
        for (int nNode : nodesToVisit) {
            if (nNextNode == -1) nNextNode = nNode;
            dSum += total[nCurNode][nNode];
            // if the value of p is greater than the average value, the node is selected
            if (dSum >= p) {
                //System.out.println("Found");
                nNextNode = nNode;
                break;
            }
        }

        return nNextNode;
    }

    public void localUpdatingRule(int curNode, int newNode) {

    }

    protected void globalUpdatingRule() {
        // 挥发
        for (int i = 0; i < m_nNodes; i++) {
            for (int j = 0; j < i; j++) {
                setTau(i, j, (1 - A) * m_tau[i][j]);
            }
        }
        for (int i = 0; i < m_nAnts.length; i++) {
            double deltaTau = 1.0 / values[i];
            for (int j = 0; j < m_nNodes; j++) {
                int r = m_nAnts[i].get(j);
                int s = m_nAnts[i].get(j + 1);
                setTau(r, s, m_tau[r][s] + deltaTau);
            }
        }
    }

    public List<Integer> cloneList(List<Integer> m_ant) {
        List<Integer> copy = new ArrayList<>(m_ant.size());
        for (int a : m_ant)
            copy.add(a);
        return copy;
    }

    // compute the length of the path
    protected int computePathValue(final List<Integer> pathVect) {
        int pathValue = 0;
        for (int i = 0; i < pathVect.size() - 1; i++) {
            pathValue += m_distance[pathVect.get(i)][pathVect.get(i + 1)];
        }
        return pathValue;
    }

    protected List<Integer> constructSolution() {
        Set<Integer> nodesToVisit = new HashSet(m_nNodes);
        for (int i = 1; i < m_nNodes; i++)
            nodesToVisit.add(i);

        int curNode = 0;
        List<Integer> curTour = new ArrayList<>(m_nNodes + 1);
        curTour.add(curNode);
        // repeat while all nodes has been visited.
        while (!nodesToVisit.isEmpty()) {

            // apply the State Transition Rule
            int newNode = stateTransitionRule(curNode, nodesToVisit);

            // delete the selected node from the list of node to visit
            nodesToVisit.remove(newNode);

            // add the new node to the list
            curTour.add(newNode);

            // apply the Local Updating Rule
            localUpdatingRule(curNode, newNode);

            curNode = newNode;
        }
        // return home
        curTour.add(0);

        // apply the Local Updating Rule
        localUpdatingRule(curNode, 0);

        return curTour;
    }

    @Override
    public void nextEpoch() {

        // construct new path
        for (int i = 0; i < m_nAnts.length; i++) {
            m_nAnts[i] = constructSolution();
            values[i] = computePathValue(m_nAnts[i]);
        }

        // record the iter_best
        for (int i = 0; i < values.length; i++) {
            if (values[i] < iter_best_value) {
                iter_best_value = values[i];
                iter_best = cloneList(m_nAnts[i]);

                s_nLastBestPathIteration = m_nIterCounter;
//                    m_outs1.println("Ant " + ant.antID + "," + ant.pathValue + "," + m_nIterCounter + "," + ant.pathVect);
            }
        }

        // update the so_far_best
        bestUnchanged = true;
        if (iter_best_value < so_far_best_value) {
            so_far_best_value = iter_best_value;
            so_far_best = cloneList(iter_best);
            bestUnchanged = false;
        }

        // record each iter result
//            m_outs2.println(m_nIterCounter + ";" + so_far_best.pathValue + ";" + m_graph.averageTau());

        // apply global updating rule
        globalUpdatingRule();
    }

    @Override
    public boolean bestUnchanged() {
        return bestUnchanged;
    }

    @Override
    public double getBestValue() {
        return so_far_best_value;
    }

    @Override
    public List<Integer> getBestTour() {
        return so_far_best;
    }

}
