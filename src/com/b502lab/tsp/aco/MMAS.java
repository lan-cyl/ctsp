package com.b502lab.tsp.aco;

import java.util.Set;

import static com.b502lab.tsp.common.Constant.A;
import static com.b502lab.tsp.common.Constant.RANDOM_GEN;

/**
 * Created by lan_cyl on 2017/1/3.
 */
public class MMAS extends AS {
    public MMAS(int n, int[][] dis) {
        super(n, dis);
    }

    @Override
    public int stateTransitionRule(int nCurNode, Set<Integer> nodesToVisit) {
        double dSum = 0;
        int nMaxNode = -1;

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
            if (nMaxNode == -1) nMaxNode = nNode;
            dSum += total[nCurNode][nNode];
            // if the value of p is greater than the average value, the node is selected
            if (dSum >= p) {
                //System.out.println("Found");
                nMaxNode = nNode;
                break;
            }
        }

        return nMaxNode;
    }

    @Override
    public void localUpdatingRule(int r, int s) {

    }

    @Override
    protected void globalUpdatingRule() {
        // 挥发
        for (int i = 0; i < m_nNodes; i++) {
            for (int j = 0; j <= i; j++) {
                setTau(i, j, (1 - A) * m_tau[i][j]);
            }
        }

        // update iteration best route
        updateBest();

        // bound the min and max tau
        double tauMax = 1. / (A * so_far_best_value);
        double tauMin = tauMax / (2. * m_nNodes);
        for (int i = 0; i < m_nNodes; i++) {
            for (int j = 0; j <= i; j++) {
                if (m_tau[i][j] < tauMin) {
                    setTau(i, j, tauMin);
                } else if (m_tau[i][j] > tauMax) {
                    setTau(i, j, tauMax);
                }
            }
        }

    }

    private void updateBest() {

        double deltaTau = 1.0 / iter_best_value;
        for (int i = 0; i < m_nNodes; i++) {
            int r = iter_best.get(i);
            int s = iter_best.get(i + 1);
            setTau(r, s, m_tau[r][s] + deltaTau);
        }
    }
}
