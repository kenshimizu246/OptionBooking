package com.tamageta.financial.booking.rfq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.StrategyLeg;

public class OptionBreakEvenGraphImpl extends JPanel {
	private Vector<Option> options = new Vector<Option>();
	private int offsetX = 10;
	private int offsetY = 10;
	
	public static final int CALL  = +1;
	public static final int PUT   = -1;
	public static final int LONG  = +1;
	public static final int SHORT = -1;
	
	private static final Color[] colors = new Color[]{
		Color.RED,
		Color.BLUE,
		Color.GREEN,
		Color.YELLOW,
		Color.CYAN,
		Color.MAGENTA,
		Color.PINK,
		Color.DARK_GRAY
	};
	
	private class Option{
		private Option(int pos, int cp, double premium, double strike){
			this.pos = pos;
			this.cp = cp;
			this.premium = premium;
			this.strike  = strike;
		}
		final int pos;
		final int cp;
		final double premium;
		final double strike;
	}
	
	public void addOption(int position, int cp, double premium, double strike){
		if(cp != CALL && cp != PUT){
			throw new IllegalArgumentException("First argument must be CALL("+CALL+") or PUT("+PUT+")!");
		}
		options.add(new Option(position, cp, premium, strike));
	}
	
	public void removeAllOptions(){
		options.removeAllElements();
		updateUI();
	}

	public void paintComponent(Graphics g) {
		Color fg = g.getColor();
		g.clearRect(0, 0, getWidth(), getHeight());
		try{
			int w = getWidth()-(offsetX*2);
			int h = getHeight()-(offsetY*2);
			int c = h/2;
			int sp = w/2+offsetX;
			
			g.setColor(Color.BLACK);
			g.drawRect(offsetX, offsetY, w, h);
			g.drawLine(0+offsetX, c+offsetY, w+offsetX, c+offsetY); // draw center line
			
			double minPrem = Double.MAX_VALUE;
			double maxPrem = 0;
			double avgStrk = 0;
			
			for(Option o : options){
				minPrem = Math.min(o.premium, minPrem);
				maxPrem = Math.max(o.premium, maxPrem);
				avgStrk += o.strike;
			}
			avgStrk = avgStrk / options.size();
			
			double unit = (maxPrem * 3) / (Math.min(h,w)/2);//c;
			int as = (int)(avgStrk/unit);
			int adjustX = w/2-as;
		
			for(int i = 0; i < options.size(); i++){
				Option o = options.get(i);
				g.setColor(colors[i]);
				
				int pp = (int)(o.premium / unit);
				int ss = (int)(o.strike / unit);
				int x1, x2, x3, y1, y2, y3 = -1;
				int bep = -1; // break even point

				if(o.cp == CALL){
					if(o.pos > 0){ // call long
						x1 = 0;
						y1 = c + pp;
						x2 = ss+adjustX;
						y2 = y1;
						x3 = x2 + y2;
						y3 = 0;
						if(x3 > w){
							x3 = w;
							y3 = y2 - (w - x2);
						}
						bep = x2 + pp;
					}else{ // call short
						x1 = 0;
						y1 = c - pp;
						x2 = ss+adjustX;
						y2 = y1;
						x3 = x2 + (h - y2);
						y3 = h;
						if(x3 > w){
							x3 = w;
							y3 = y2 + (w - x2);
						}
						bep = x2 + pp;
					}
				}else{
					if(o.pos > 0){ // call long
						x1 = w;
						y1 = c + pp;
						x2 = ss+adjustX;
						y2 = y1;
						x3 = x2 - y2;
						y3 = 0;
						if(x3 < 0){
							x3 = 0;
							y3 = y2 - x2;
						}
						bep = x2 - pp;
					}else{
						x1 = w;
						y1 = c - pp;
						x2 = ss+adjustX;
						y2 = y1;
						x3 = x2 - (h - y2);
						y3 = h;
						if(x3 < 0){
							x3 = 0;
							y3 = y2 + x2;
						}
						bep = x2 - pp;
					}
				}
				g.drawLine(x1+offsetX,y1+offsetY,x2+offsetX,y2+offsetY);
				g.drawLine(x2+offsetX,y2+offsetY,x3+offsetX,y3+offsetY);
				g.drawLine(bep+offsetX, c+offsetY-2, bep+offsetX, c+offsetY+2);
			}
		}finally{
			g.setColor(fg);
		}
	}

	public void setRFQ(Quote rfq){
		if(rfq != null){
			List<StrategyLeg> legs = rfq.getStrategies();
			int pos = (Quote.BUY.equals(rfq.getBuyOrSell()) ? LONG : SHORT);
			removeAllOptions();
			for(StrategyLeg l : legs){
				int cp = (StrategyLeg.CALL.equals(l.getCallOrPut())?CALL:PUT);
				addOption((StrategyLeg.LONG.equals(l.getLs())
						? pos : pos * -1)
						, cp
						, l.getPremium()
						, l.getStrike());
			}
		}else{
			removeAllOptions();
		}
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				updateUI();
			}
		});
	}
	
	public static void main(String[] args) {
		OptionBreakEvenGraphImpl p = new OptionBreakEvenGraphImpl();
		
		//p.addOption(LONG, CALL, 20, 100);
		//p.addOption(LONG, CALL, 25, 120);
		//p.addOption(SHORT, CALL, 20, 100);
		//p.addOption(SHORT, CALL, 25, 120);
		//p.addOption(LONG, PUT, 20, 100);
		//p.addOption(LONG, PUT, 25, 120);
		//p.addOption(SHORT, PUT, 20, 100);
		//p.addOption(SHORT, PUT, 25, 120);
		//p.addOption(LONG, PUT, 20, 100);
		//p.addOption(SHORT, CALL, 20, 100);
		//p.addOption(SHORT, PUT, 20, 60);
		//p.addOption(SHORT, PUT, 20, 80);
		//p.addOption(SHORT, PUT, 20, 100);
		
		// Straddle
//		p.addOption(LONG, CALL, 20, 100);
//		p.addOption(LONG, PUT,  20, 100);

		// Strangle
		p.addOption(LONG, CALL, 20, 110);
		p.addOption(LONG, PUT,  20, 90);

		p.setForeground(Color.WHITE);
		p.setPreferredSize(new Dimension(300,300));
		
		JPanel pp = new JPanel();
		JButton bb = new JButton("Paint");
		bb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		
		JFrame f = new JFrame("OptionBreakEvenGraph Test");
		//f.getContentPane().setLayout(new BorderLayout());
		//f.getContentPane().add(p, BorderLayout.CENTER);
		f.getContentPane().add(p);
		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

}
