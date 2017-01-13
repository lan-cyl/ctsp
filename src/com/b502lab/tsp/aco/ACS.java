package com.b502lab.tsp.aco;

import com.b502lab.tsp.aco.AS;

import java.util.Set;

import static com.b502lab.tsp.common.Constant.*;

public class ACS extends AS {

    public ACS(int n, int[][] dis) {
        super(n, dis);
    }

    public int stateTransitionRule(int nCurNode, Set<Integer> nodesToVisit) {
        // generate a random number
        double q = RANDOM_GEN.nextDouble();
        int nMaxNode = -1;

        if (q <= Q0)  // Exploitation
        {
//            System.out.print("Exploitation: ");
            double dMaxVal = -1;
            double dVal;

            // search the max of the value as defined in Eq. a)
            for (int nNode : nodesToVisit) {
                // check on tau
                if (m_tau[nCurNode][nNode] == 0)
                    throw new RuntimeException("tau = 0");

                // get the value
                dVal = total[nCurNode][nNode];

                // check if it is the max
                if (dVal > dMaxVal) {
                    dMaxVal = dVal;
                    nMaxNode = nNode;
                }
            }
        } else  // Exploration
        {
//              System.out.println("Exploration");
            double dSum = 0;

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
        }

        if (nMaxNode < 0)
            throw new RuntimeException("maxNode = -1");

        return nMaxNode;
    }


    @Override
    public void localUpdatingRule(int nCurNode, int nNextNode) {
        // get the value of the Eq. c)
        double val = (1 - R) * m_tau[nCurNode][nNextNode] +
                R * m_dTau0;

        // update tau
        setTau(nCurNode, nNextNode, val);
    }

    @Override
    protected void globalUpdatingRule() {
        // 挥发
        for (int i = 0; i < m_nNodes; i++) {
            for (int j = 0; j < i; j++) {
//                setTau(i, j, (1 - A) * m_tau[i][j]);
            }
        }
        // 最优解更新
        updateBest();
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
