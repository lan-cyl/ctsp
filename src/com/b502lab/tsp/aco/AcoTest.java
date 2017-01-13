package com.b502lab.tsp.aco;

import static com.b502lab.tsp.common.Constant.ALL_TSP_PATH;
import static com.b502lab.tsp.common.Tsp.me;

public class AcoTest {

    protected static String tspFilename = "";
    protected static String methodName = "";
    protected static int nRepetitions = 0;

    public static void main(String[] args) {
        // Print application prompt to console.
        System.out.println("AntColonySystem for Tsp");

        if (args.length < 8) {
            System.out.println("Wrong number of parameters");
            return;
        }

        initParams(args);

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
            } else if (args[i].equals("-r")) {
                nRepetitions = Integer.parseInt(args[i + 1]);
                System.out.println("Repetitions: " + nRepetitions);
            }
        }
    }

    protected static void run() {
        me.init(ALL_TSP_PATH + tspFilename);

        for (int i = 0; i < nRepetitions; i++) {
            AS antColony = selectMethod(methodName);
            antColony.execute();
            System.out.println("so_far_best value: " + antColony.so_far_best_value);
        }
    }

    protected static AS selectMethod(String methodName) {
        switch (methodName) {
            case "as":
                return new AS(me.n, me.distance);
            case "impl":
                return new ACS(me.n, me.distance);
            case "mmas":
                return new MMAS(me.n, me.distance);
            default:
                return new ACS(me.n, me.distance);
        }
    }
}

