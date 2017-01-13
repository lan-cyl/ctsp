package com.b502lab.ctsp.aco;

import com.b502lab.tsp.aco.MMAS;

import java.util.List;

/**
 * Created by lan_cyl on 2017/1/4.
 */
public class MMAS4Ctsp extends MMAS {

    public MMAS4Ctsp(int n, int[][] dis) {
        super(n, dis);
    }

    @Override
    public List<Integer> constructSolution() {
        return ConstructSolution.constructSolution(this);
    }
}
