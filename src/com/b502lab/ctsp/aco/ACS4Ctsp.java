package com.b502lab.ctsp.aco;

import com.b502lab.tsp.aco.ACS;

import java.util.List;

/**
 * Created by lan_cyl on 2017/1/4.
 */
public class ACS4Ctsp extends ACS {

    public ACS4Ctsp(int n, int[][] dis) {
        super(n, dis);
    }

    @Override
    protected List<Integer> constructSolution() {
        return ConstructSolution.constructSolution(this);
//        LinkedList<Integer> curTour = new LinkedList<>();// 当前微粒构造的调度方案
//        UpdateSolution.updatePath(s_antColony.getGraph().m_tau, curTour);
//        for (int i = 0; i < pathVect.length; i++) {
//            pathVect[i] = curTour.get(i);
//        }
    }
}
