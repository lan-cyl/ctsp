package com.b502lab.tsp.common;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by lan_cyl on 2016/12/29.
 */
public class Tsp {
    public final static Tsp me = new Tsp();

    protected String comment;
    protected String type;
    protected String edge_weight_type;
    public int[][] position;// 各节点的坐标

    public String name;// 问题名称
    public int n;// 节点个数
    public int[][] distance;// 节点间距离
    public int[][] nn_list;// 近邻
    public int nn_ls;

    /**
     * 读取TSP文件，文件的格式定义参见TSPLIB.
     * <p>
     * FUNCTION: parse and read me file INPUT: me name OUTPUT: list
     * of coordinates for all nodes COMMENTS: Instance files have to be in
     * TSPLIB format, otherwise procedure fails
     */
    public void init(final String tsp_file_name) {
        Scanner read_tsp_file;
        String buf = "";

        try {
            read_tsp_file = new Scanner(new FileReader(tsp_file_name));
        } catch (FileNotFoundException e) {
            System.out.println("无法读取TSP文件，请检查文件是否有效：" + tsp_file_name);
            System.exit(1);
            return;
        }

        System.out.println("reading impl-file " + tsp_file_name + " ...");

        while ((buf = read_tsp_file.next()) != null
                && !buf.equals("NODE_COORD_SECTION")) {
            if (buf.equals("NAME")) {
                buf = read_tsp_file.next();
                buf = read_tsp_file.next();
                name = new String(buf);
            } else if (buf.equals("NAME:")) {
                buf = read_tsp_file.next();
                name = new String(buf);
            } else if (buf.equals("COMMENT")) {
                buf = read_tsp_file.next();
                buf = read_tsp_file.next();
                comment = new String(buf);
            } else if (buf.equals("COMMENT:")) {
                buf = read_tsp_file.next();
                comment = new String(buf);
            } else if (buf.equals("TYPE")) {
                buf = read_tsp_file.next();
                buf = read_tsp_file.next();
                if (!buf.equals("TSP")) {
                    System.err
                            .print("\n Not a Tsp me in TSPLIB format !!\n");
                    System.exit(1);
                }
                type = new String(buf);
            } else if (buf.equals("TYPE:")) {
                buf = read_tsp_file.next();
                if (!buf.equals("TSP")) {
                    System.err
                            .print("\n Not a Tsp me in TSPLIB format !!\n");
                    System.exit(1);
                }
                type = new String(buf);
            } else if (buf.equals("DIMENSION")) {
                buf = read_tsp_file.next();
                n = read_tsp_file.nextInt();
            } else if (buf.equals("DIMENSION:")) {
                n = read_tsp_file.nextInt();
            } else if (buf.equals("EDGE_WEIGHT_TYPE")) {
                buf = read_tsp_file.next();
                buf = read_tsp_file.next();
                if (buf.equals("EUC_2D")) {
                    edge_weight_type = "EUC_2D";
                } else if (buf.equals("CEIL_2D")) {
                    edge_weight_type = "CEIL_2D";
                } else if (buf.equals("GEO")) {
                    edge_weight_type = "GEO";
                } else if (buf.equals("ATT")) {
                    edge_weight_type = "ATT";
                } else {
                    System.err.printf("EDGE_WEIGHT_TYPE %s not implemented\n",
                            buf);
                    System.exit(1);
                }
                edge_weight_type = new String(buf);
            } else if (buf.equals("EDGE_WEIGHT_TYPE:")) {
                buf = read_tsp_file.next();
                if (buf.equals("EUC_2D")) {
                    edge_weight_type = "EUC_2D";
                } else if (buf.equals("CEIL_2D")) {
                    edge_weight_type = "CEIL_2D";
                } else if (buf.equals("GEO")) {
                    edge_weight_type = "GEO";
                } else if (buf.equals("ATT")) {
                    edge_weight_type = "ATT";
                } else {
                    System.err.printf("EDGE_WEIGHT_TYPE %s not implemented\n",
                            buf);
                    System.exit(1);
                }
            }
        }

        if (!buf.equals("NODE_COORD_SECTION")) {
            System.err
                    .println("\n\nSome error ocurred finding start of coordinates from impl file !!");
            System.exit(1);
        }

        position = new int[n][2];
        for (int i = 0; i < n; i++) {
            read_tsp_file.next();
            position[i][0] = read_tsp_file.nextInt();
            position[i][1] = read_tsp_file.nextInt();
        }
        System.out.println("reading impl-file complete!!!");
        read_tsp_file.close();

        // 计算节点间距离
        compute_distances();
    }

    /**
     * FUNCTION: computes the matrix of all intercity distances // 计算所有城市间的距离
     */
    protected void compute_distances() {
        int[][] matrix = new int[n][n];

        if (edge_weight_type.equals("EUC_2D")) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    matrix[i][j] = round_distance(i, j);
                }
            }
        } else if (edge_weight_type.equals("CEIL_2D")) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    matrix[i][j] = ceil_distance(i, j);
                }
            }
        } else if (edge_weight_type.equals("GEO")) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    matrix[i][j] = geo_distance(i, j);
                }
            }
        } else if (edge_weight_type.equals("ATT")) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    matrix[i][j] = att_distance(i, j);
                }
            }
        }
        distance = matrix;
        init_nn_list();
    }

    private void init_nn_list() {
        nn_ls = n < 21 ? n - 1 : 20;
        nn_list = new int[n][nn_ls];
        for (int i = 0; i < n; i++) {
            nn_list[i] = getNN(i);
        }
    }

    private int[] getNN(int i) {
        int[] nn = new int[nn_ls];
        int[] a = Arrays.copyOf(distance[i], n);
        a[i] = Integer.MAX_VALUE;
        for (int j = 0; j < nn_ls; j++) {
            int idx = j;
            for (int k = j + 1; k < n; k++)
                if (k != i && a[k] < a[idx])
                    idx = k;
            swap(a, j, idx);
            nn[j] = idx;
        }
        return nn;
    }

    private void swap(int[] a, int j, int idx) {
        int t = a[j];
        a[j] = a[idx];
        a[idx] = t;
    }

    /**
     * FUNCTION: Weights are Euclidean distance in 2-D // 四舍五入取整
     * <p>
     * COMMENTS: for the definition of how to compute this distance see TSPLIB
     * <p>
     * INPUT: two node indices
     *
     * @param i
     * @param j
     * @return distance between the two nodes
     */
    int round_distance(int i, int j) {
        double xd = position[i][0] - position[j][0];
        double yd = position[i][1] - position[j][1];
        double r = Math.sqrt(xd * xd + yd * yd) + 0.5;

        return (int) r;
    }

    /**
     * FUNCTION: Weights are Euclidean distance in 2-D rounded up. The edge
     * weight type CEIL_2D requires the 2-dimensional Euclidean distances is
     * rounded up to the next integer.
     * <p>
     * INPUT: two node indices
     * <p>
     * COMMENTS: for the definition of how to compute this distance see TSPLIB
     *
     * @param i
     * @param j
     * @return distance between the two nodes
     */
    int ceil_distance(int i, int j) {
        double xd = position[i][0] - position[j][0];
        double yd = position[i][1] - position[j][1];

        double r = Math.sqrt(xd * xd + yd * yd); // 上限取整
        double t = dtrunc(r);
        if (t < r) {
            return (int) (t + 1);
        } else {
            return (int) t;
        }
    }

    /**
     * FUNCTION: Weights are geographical distances.
     * <p>
     * INPUT: two node indices
     * <p>
     * OUTPUT: distance between the two nodes
     * <p>
     * COMMENTS: adapted from concorde code for the definition of how to compute
     * this distance see TSPLIB
     */
    int geo_distance(int i, int j) {
        double deg, min;
        double lati, latj, longi, longj;
        double x1 = position[i][0], x2 = position[j][0];
        double y1 = position[i][1], y2 = position[j][1];

        deg = dtrunc(x1); // 得到x1的整数部分，即度数
        min = x1 - deg; // 得到min的小数部分，即分
        lati = Math.PI * (deg + 5.0 * min / 3.0) / 180.0; // 计算得到纬度
        deg = dtrunc(x2);
        min = x2 - deg;
        latj = Math.PI * (deg + 5.0 * min / 3.0) / 180.0;

        deg = dtrunc(y1);
        min = y1 - deg;
        longi = Math.PI * (deg + 5.0 * min / 3.0) / 180.0; // 计算得到经度
        deg = dtrunc(y2);
        min = y2 - deg;
        longj = Math.PI * (deg + 5.0 * min / 3.0) / 180.0;

        double q1 = Math.cos(longi - longj);
        double q2 = Math.cos(lati - latj);
        double q3 = Math.cos(lati + latj);
        double dij = 6378.388 * Math.acos(0.5 * ((1.0 + q1) * q2 - (1.0 - q1)
                * q3)) + 1.0;
        return (int) dij;

    }

    /**
     * FUNCTION: Special distance function for problems att48 and att532 The
     * edge weight type ATT corresponds to a special "pseudo-Enclidean" distance
     * function.
     * <p>
     * INPUT: two node indices
     * <p>
     * OUTPUT: distance between the two nodes
     * <p>
     * COMMENTS: for the definition of how to compute this distance see TSPLIB
     */
    int att_distance(int i, int j) {
        double xd = position[i][0] - position[j][0];
        double yd = position[i][1] - position[j][1];
        double rij = Math.sqrt((xd * xd + yd * yd) / 10.0);
        double tij = dtrunc(rij);

        if (tij < rij) {
            return (int) (tij + 1);
        } else {
            return (int) tij;
        }
    }

    /**
     * FUNCTION: 下限取整
     */
    double dtrunc(double x) {

        int k = (int) x;
        x = (double) k;
        return x;
    }
}
