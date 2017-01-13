/*
 * Copyright (C) 2007-2016 Ugo Chirico
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Affero GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.b502lab.ctsp.aco;

import com.b502lab.tsp.aco.AS;

import static com.b502lab.ctsp.common.Ctsp.me;
import static com.b502lab.tsp.common.Constant.ALL_CTSP_PATH;

public class Test {

    private static String tspFilename = "eil51-4.ctsp";
    private static String methodName = "acs";
    private static int nAnts = 30;
    private static int nIterations = 2500;
    private static int nRepetitions = 10;

    public static void main(String[] args) {
        // Print application prompt to console.
        System.out.println("AntColonySystem for Tsp");

        if (args.length < 8) {
            System.out.println("Wrong number of parameters");
            return;
        }

        initParams(args);
        if (nAnts == 0 || nIterations == 0 || nRepetitions == 0) {
            System.out.println("One of the parameters is wrong");
            return;
        }

        run();
    }

    protected static void initParams(String[] args) {
        for (int i = 0; i < args.length; i += 2) {
            if (args[i].equals("-f")) {
                tspFilename = args[i + 1];
                System.out.println("Tsp file name: " + tspFilename);
            } else if (args[i].equals("-m")) {
                methodName = args[i + 1];
                System.out.println("Method: " + methodName);
            } else if (args[i].equals("-a")) {
                nAnts = Integer.parseInt(args[i + 1]);
                System.out.println("Ants: " + nAnts);
            } else if (args[i].equals("-i")) {
                nIterations = Integer.parseInt(args[i + 1]);
                System.out.println("Iterations: " + nIterations);
            } else if (args[i].equals("-r")) {
                nRepetitions = Integer.parseInt(args[i + 1]);
                System.out.println("Repetitions: " + nRepetitions);
            }
        }
    }

    protected static void run() {
        me.init(ALL_CTSP_PATH + tspFilename);

        for (int i = 0; i < nRepetitions; i++) {
            AS antColony = selectMethod(methodName);
            antColony.execute();
            System.out.println("so_far_best value: " + antColony.so_far_best_value);
        }
    }

    protected static AS selectMethod(String methodName) {
        switch (methodName) {
            case "as":
                return new AS4Ctsp(me.n, me.distance);
            case "acs":
                return new ACS4Ctsp(me.n, me.distance);
            case "mmas":
                return new MMAS4Ctsp(me.n, me.distance);
            default:
                return new ACS4Ctsp(me.n, me.distance);
        }
    }
}

