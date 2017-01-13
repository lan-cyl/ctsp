package com.b502lab.ctsp.aco;

import com.b502lab.ctsp.ito.UpdateSolution;
import com.b502lab.tsp.aco.AS;

import java.util.List;

/**
 * Created by lan_cyl on 2017/1/4.
 */
public class AS4Ctsp extends AS {

    public AS4Ctsp(int n, int[][] distance) {
        super(n, distance);
    }

    @Override
    public List<Integer> constructSolution() {
//        return ConstructSolution.constructSolution(this);
        return UpdateSolution.updatePath(m_tau);
    }
}
