package com.b502lab.ctsp.common;

import com.b502lab.tsp.common.Tsp;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 * Created by lan_cyl on 2016/12/29.
 */
public class Ctsp extends Tsp {
    public final static Ctsp me = new Ctsp();

    public int salesmen_count;
    public int[] colors;// 每个城市的颜色，0表示没有颜色的共享城市
    public int[] colorsCount;

    public final int DEPORT = 0;// 仓库位置

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

        System.out.println("reading ctsp-file " + tsp_file_name + " ...");

        while ((buf = read_tsp_file.next()) != null
                && !buf.equals("NODE_COORD_SECTION")) {
            if (buf.equals("NAME:")) {
                buf = read_tsp_file.next();
                name = new String(buf);
            } else if (buf.equals("COMMENT:")) {
                buf = read_tsp_file.next();
                comment = new String(buf);
            } else if (buf.equals("TYPE:")) {
                buf = read_tsp_file.next();
                if (!buf.equals("CTSP")) {
                    System.err
                            .print("\n Not a Ctsp me in CTSPLIB format !!\n");
                    System.exit(1);
                }
                type = new String(buf);
            } else if (buf.equals("DIMENSION:")) {
                n = read_tsp_file.nextInt();
            } else if (buf.equals("SALESMEN:")) {
                String[] salesmen = read_tsp_file.next().split("\\(");// 旅行商个数（第一组个数，第二组，。。。，公共个数）
                salesmen_count = Integer.parseInt(salesmen[0]);
                colorsCount = new int[salesmen_count + 1];
                colors = new int[n];
                colors[0] = 0;// deport 0 is common, last node is common, others have exclusive color
                String[] tmp = salesmen[1].substring(0, salesmen[1].length() - 1).split(",");
                for (int i = 0, k = 1, color = 1; i < tmp.length - 1; i++, color++) {
                    int num = Integer.parseInt(tmp[i]);
                    colorsCount[color] = num;
                    for (int j = 0; j < num; j++) {
                        colors[k++] = color;
                    }
                }
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
                    .println("\n\nSome error ocurred finding start of coordinates from ctsp file !!");
            System.exit(1);
        }

        position = new int[n][2];
        for (int i = 0; i < n; i++) {
            read_tsp_file.next();
            position[i][0] = read_tsp_file.nextInt();
            position[i][1] = read_tsp_file.nextInt();
        }
        System.out.println("reading ctsp-file complete!!!");
        read_tsp_file.close();

        // 计算节点间距离
        compute_distances();
    }

    public boolean canVisit(int i, int j) {
        if (i == j) return false;
        int colori = Ctsp.me.colors[i];
        int colorj = Ctsp.me.colors[j];
        return colori == colorj || colori == 0 || colorj == 0;
    }
}
