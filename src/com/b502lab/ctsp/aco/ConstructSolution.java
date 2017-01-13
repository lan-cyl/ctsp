package com.b502lab.ctsp.aco;

import com.b502lab.ctsp.common.Ctsp;
import com.b502lab.tsp.aco.AS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lan_cyl on 2017/1/4.
 */
public class ConstructSolution {

    public static ArrayList<Integer> constructSolution(AS ant) {
        Set<Integer> nodesToVisit = new HashSet(Ctsp.me.n);
        for (int i = 1; i < Ctsp.me.n; i++)
            nodesToVisit.add(i);

        int[][] tours = new int[Ctsp.me.salesmen_count + 1][Ctsp.me.n];
        int[] curNodesIdx = new int[tours.length];
        // repeat while End of Activity Rule returns false
        while (!nodesToVisit.isEmpty()) {
            for (int color = 1; color <= Ctsp.me.salesmen_count; color++) {
                // apply the State Transition Rule
                int newNode = neighbourChoose(color, ant, tours, curNodesIdx, nodesToVisit);

                if (newNode != 0) {// now i can go home, but i select observe, maybe i want to visit other city next time.
                    curNodesIdx[color] += 1;
                    tours[color][curNodesIdx[color]] = newNode;

                    // delete the selected node from the list of node to visit
                    nodesToVisit.remove(newNode);
                }
            }
        }

        // get path vector
        return combinePath(tours);
    }

    private static ArrayList<Integer> combinePath(int[][] tours) {
        ArrayList<Integer> curTour = new ArrayList<>();
        for (int i = 1; i < tours.length; i++) {
            curTour.add(0);
            for (int j = 1; j < tours[i].length && tours[i][j] != 0; j++) {
                curTour.add(tours[i][j]);
            }
        }
        curTour.add(0);
        return curTour;
    }

    private static int neighbourChoose(int color, AS ant, int[][] tours, int[] curNodesIdx, Set<Integer> nodesToVisit) {
        HashSet<Integer> nodesCanVisit = allNodesCanVisit(color, nodesToVisit);

        if (nodesCanVisit.isEmpty()) return 0;
        // this colored salesman has no city to go.

        if (sameColorNodes(color, nodesToVisit).isEmpty() && !nodesToVisitIsAllCommon(nodesToVisit))
            nodesCanVisit.add(0);
        // i have visit all the cities colored same with me,
        // and other salesman haven't complete their visit, then i can select go home;
        // but if other salesman also have complete their colored visit, that is to say all node need to visit is common,
        // then we have to select one common city to visit other than go home.

        int nCurNode = tours[color][curNodesIdx[color]];

        int nNewNode = ant.stateTransitionRule(nCurNode, nodesCanVisit);
        if (nNewNode != 0)
            ant.localUpdatingRule(nCurNode, nNewNode);

        return nNewNode;
    }

    private static boolean nodesToVisitIsAllCommon(Set<Integer> nodesToVisit) {
        for (int a : nodesToVisit) {
            if (Ctsp.me.colors[a] != 0)
                return false;
        }
        return true;
    }

    private static HashSet<Integer> allNodesCanVisit(int color, Set<Integer> nodesToVisit) {
        HashSet<Integer> set = new HashSet<>();
        for (int a : nodesToVisit) {
            if (Ctsp.me.colors[a] == color || Ctsp.me.colors[a] == 0) {
                set.add(a);
            }
        }
        return set;
    }

    private static Set<Integer> sameColorNodes(int color, Set<Integer> nodesToVisit) {
        Set<Integer> set = new HashSet<>();
        for (int a : nodesToVisit)
            if (Ctsp.me.colors[a] == color) set.add(a);

        return set;
    }

}
