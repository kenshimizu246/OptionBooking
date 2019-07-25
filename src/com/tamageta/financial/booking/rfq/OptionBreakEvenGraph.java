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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;

import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.StrategyLeg;

public class OptionBreakEvenGraph extends JPanel {
	private Vector<Option> options = new Vector<Option>();
	private JFreeChart chart;
	
	private static final int CALL  = +1;
	private static final int PUT   = -1;
	private static final int LONG  = +1;
	private static final int SHORT = -1;
	
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
	
	public OptionBreakEvenGraph(){
		XYSeriesCollection dataset = new XYSeriesCollection();
		chart = ChartFactory.createXYLineChart
		  ("XYLine Chart using JFreeChart", "Age", "Weight",
				  dataset, PlotOrientation.VERTICAL, true, true, false);
	}
	
	public void setRFQ(Quote rfq){
		if(rfq != null){
			List<StrategyLeg> legs = rfq.getStrategies();
			int pos = (Quote.BUY.equals(rfq.getBuyOrSell()) ? LONG : SHORT);
			options.removeAllElements();
			for(StrategyLeg l : legs){
				int cp = (StrategyLeg.CALL.equals(l.getCallOrPut())?CALL:PUT);
				options.add(new Option(
						(StrategyLeg.LONG.equals(l.getLs()) ? pos : pos * -1),
						cp, 
						l.getPremium(), 
						l.getStrike()));
			}
		}else{
			options.removeAllElements();
		}
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				updateUI();
			}
		});
	}
	
	public static void main(String[] args) {
		OptionBreakEvenGraph p = new OptionBreakEvenGraph();
		
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
		//p.addOption(LONG, CALL, 20, 110);
		//p.addOption(LONG, PUT,  20, 90);

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
