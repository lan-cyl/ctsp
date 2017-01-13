package com.b502lab.tsp.ga;

import com.b502lab.tsp.common.Tsp;

import static com.b502lab.tsp.common.Constant.ALL_TSP_PATH;
import static com.b502lab.tsp.common.Tsp.me;

public class GaTest {

    protected static String tspFilename = "";
    protected static String methodName = "";
    protected static int nIterations = 0;
    protected static int nRepetitions = 0;

    public static void main(String[] args) {
        // Print application prompt to console.
        System.out.println("Genetic SimpleGA for Tsp");

        if (args.length < 8) {
            System.out.println("Wrong number of parameters");
            return;
        }

        initParams(args);

        if (nIterations == 0 || nRepetitions == 0) {
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
        me.init(ALL_TSP_PATH + tspFilename);

        for (int i = 0; i < nRepetitions; i++) {
            ImproveGA GA = selectMethod(methodName);
            GA.start();
            System.out.println(i + "," + GA.bestValue + "," + GA.best);
        }
    }

    protected static ImproveGA selectMethod(String methodName) {
        switch (methodName) {
            case "ga":
                return new ImproveGA(Tsp.me.n, Tsp.me.distance);
            case "gag":
            case "hcga":
            case "saga":
            default:
                return new ImproveGA(Tsp.me.n, Tsp.me.distance);
        }
    }
}

