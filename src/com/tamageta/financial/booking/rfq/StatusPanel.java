package com.tamageta.financial.booking.rfq;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class StatusPanel extends JPanel {
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int y = 0;
		g.setColor(new Color(156,154,140));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(196,194,183));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(218,215,201));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(233,231,217));
		g.drawLine(0, y, getWidth(), y);

		y = getHeight() - 3;
		g.setColor(new Color(233,232,218));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(233,231,216));
		g.drawLine(0, y, getWidth(), y);
		y = getHeight() - 1;
		g.setColor(new Color(221,221,220));
		g.drawLine(0, y, getWidth(), y);
	}
	private class SeparatorPanel extends JPanel {
		private Color leftColor;
		private Color rightColor;
		
		public SeparatorPanel(Color left, Color right){
			this.leftColor = left;
			this.rightColor = right;
			setOpaque(false);
		}
		
		protected void paintConponent(Graphics g){
			g.setColor(leftColor);
			g.drawLine(0,0,0,getHeight());
			g.setColor(rightColor);
			g.drawLine(1,0,1,getHeight());
		}
	}
}
