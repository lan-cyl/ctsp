package com.b502lab.ctsp.draw;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Polygon;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class CtspResultFrame extends JFrame {

	public CtspResultFrame() throws HeadlessException {
		super();
		initPanel();
	}

	/**
	 * 
	 * @param x
	 *            一维数组
	 * @param y
	 *            一维数组，表示坐标点
	 * @throws HeadlessException
	 */
	public CtspResultFrame(double[] x, double[] y) throws HeadlessException {
		super();
		initPanel(x, y);
	}

	public CtspResultFrame(String arg0, double[] x, double[] y) throws HeadlessException {
		super(arg0);
		initPanel(x, y);
	}

	private void initPanel() {

		this.setSize(900, 800);
		this.setVisible(true);
		this.setDefaultCloseOperation(3);
		this.getContentPane().add(new CTSPResultPanel());
	}

	private void initPanel(double[] x, double[] y) {

		this.setSize(900, 800);
		this.setVisible(true);
		this.setDefaultCloseOperation(3);
		this.getContentPane().add(new CTSPResultPanel(x, y));
	}

	public static void main(String[] args) {
		new CtspResultFrame();
	}

	/**
	 * 图像面板，显示ctsp的结果
	 * 
	 * @author lan_cyl
	 *
	 */
	class CTSPResultPanel extends JPanel {
		Polygon po = new Polygon();
		Font fn = new Font("宋体", Font.BOLD, 22);
		Font fn2 = new Font("宋体", Font.BOLD, 20);
		int x = 100;
		int y = 100;

		double[] xArrs;
		double[] yArrs;
		int minx = 0, miny = 0, maxx = 72, maxy = 72;

		public CTSPResultPanel() {
			setSize(900, 800);
		}

		public CTSPResultPanel(double[] x, double[] y) {
			setSize(900, 800);
			this.xArrs = x;
			this.yArrs = y;
			for (double d : x) {
				if (d - minx < 0)
					minx = (int) d;
				if (d - maxx > 0)
					maxx = (int) d;
			}
			for (double d : y) {
				if (d - miny < 0)
					miny = (int) d;
				if (d - maxy > 0)
					maxy = (int) d;
			}
		}

		@Override
		public void paint(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.black);

			// xy轴线段
			g2d.fillRect(99, 100, 2, 600);
			g2d.fillRect(99, 700, 600, 2);

			// 原点刻度值
			g2d.drawString(miny + "", x - 20, 705);
			g2d.drawString(minx + "", x - 5, 720);

			// 刻度值
			int scaleX = (maxx - minx + 18) / 36;
			if (scaleX < 0)
				scaleX = 1;
			int scaleY = (maxy - miny + 18) / 36;
			if (scaleY < 0)
				scaleY = 1;
			// 36条网格线
			for (int i = 1; i <= 36; i++) {
				g2d.drawLine(x + i * 15, y + 600, x + i * 15, y + 45);
				g2d.drawLine(x, y + 600 - i * 15, x + 555, y + 600 - i * 15);
				if (i % 2 == 0) {
					g2d.drawString(String.valueOf(i * scaleY + miny), x - 20, 705 - i / 2 * 30);
					g2d.drawString(String.valueOf(i * scaleX + minx), x - 5 + i / 2 * 30, 720);
				}
			}

			// 画个A
			g2d.fillRect(100, 685, 15, 15);
			g2d.setFont(fn2);
			g2d.setColor(Color.white);
			g2d.drawString("A", 102, 700);
			g2d.setFont(fn);
			g2d.setColor(Color.black);
			g2d.drawString("Y", 80, 140);
			g2d.drawString("X", 670, 720);

			// y轴箭头
			g2d.fillPolygon(new int[] { 90, 100, 100 }, new int[] { 110, 90, 100 }, 3);
			g2d.fillPolygon(new int[] { 100, 100, 110 }, new int[] { 100, 90, 110 }, 3);

			// x轴箭头
			g2d.fillPolygon(new int[] { 687, 697, 707 }, new int[] { 690, 700, 700 }, 3);
			g2d.fillPolygon(new int[] { 687, 697, 707 }, new int[] { 710, 700, 700 }, 3);

			// 画点画线
			if (xArrs != null && xArrs.length > 0) {
				double cellX = 555 / (scaleX * 37);
				double cellY = 555 / (scaleY * 37);// 每米对应的像素值
				int nPoints = xArrs.length;
				int[] xPoints = new int[nPoints];
				int[] yPoints = new int[nPoints];
				for (int i = 0; i < nPoints; i++) {
					xPoints[i] = x + (int) ((xArrs[i] - minx) * cellX);
					yPoints[i] = y + 600 - (int) ((yArrs[i] - miny) * cellY);
					g2d.drawString("*", xPoints[i] - 5, yPoints[i] + 5);
				}
				g2d.drawPolyline(xPoints, yPoints, nPoints);
			}

			g2d.dispose();
		}
	}
}