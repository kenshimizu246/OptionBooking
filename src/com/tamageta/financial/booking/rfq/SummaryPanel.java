package com.tamageta.financial.booking.rfq;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.tamageta.financial.booking.rfq.data.DataUpdateEvent;
import com.tamageta.financial.booking.rfq.data.DataUpdateHandler;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.QuoteImpl;
import com.tamageta.financial.booking.rfq.data.StrategyLeg;
import com.tamageta.financial.booking.rfq.data.StrategyLeg;
import com.tamageta.financial.booking.rfq.data.StrategyLegChangeEvent;
import com.tamageta.financial.booking.rfq.data.StrategyLegListener;

public class SummaryPanel extends JPanel implements DataUpdateHandler {
	private final JFreeChart chart;
	private final XYSeriesCollection dataset;
	private JTextArea text = new JTextArea();
	private Quote rfq = null;
	private DecimalFormat formatPremium = new DecimalFormat("0");
	private DecimalFormat formatDelta   = new DecimalFormat("0.00000");
	
	public SummaryPanel(){
		dataset = new XYSeriesCollection();
		chart = ChartFactory.createXYLineChart
		  ("", "Strike", "Profit",
				  dataset, PlotOrientation.VERTICAL, true, true, false);
		ChartPanel chartPanel = new ChartPanel(chart);
		setLayout(new GridLayout(1,2));
		add(chartPanel);
		add(new JScrollPane(text));
	}
	
	public void setRFQ(Quote rfq){
		this.rfq = rfq;
		rfq.addDataUpdateHandler(this);
		setSummaryText(rfq);
		setBreakEvenGraph(rfq);
	}
	private class NetPoint{
		private final double price;
		private double profit = 0;
		public NetPoint(double price){
			this.price = price;
		}
		public double getPrce() {
			return price;
		}
		public double getProfit() {
			return profit;
		}
		public void addProfit(double p) {
			profit = profit + p;
		}
	}
	private void setBreakEvenGraph(Quote rfq){
		if(rfq == null){
			dataset.removeAllSeries();
		}else{
			dataset.removeAllSeries();
			List<StrategyLeg> legs = new Vector<StrategyLeg>(rfq.getStrategies());
			
			Collections.sort(legs, new Comparator<StrategyLeg>(){
				@Override
				public int compare(StrategyLeg l1, StrategyLeg l2) {
					if(l1.getStrike() < l2.getStrike()){
						return -1;
					}else if(l1.getStrike() > l2.getStrike()){
						return 1;
					}
					return 0;
				}
			});

			Vector<Double> xPoints = new Vector<Double>();
			double minPrem = Double.MAX_VALUE;
			double maxPrem = 0;
			double minStrike = Double.MAX_VALUE;
			double maxStrike = 0;
			double avgStrk = 0;
			for(int i = 0; i < legs.size(); i++){
				StrategyLeg st = legs.get(i);

				xPoints.add(st.getStrike());
				
				minPrem = Math.min(st.getPremium(), minPrem);
				maxPrem = Math.max(st.getPremium(), maxPrem);
				minStrike = Math.min(st.getStrike(), minStrike);
				maxStrike = Math.max(st.getStrike(), maxStrike);
				avgStrk += st.getStrike();
			}
			avgStrk = avgStrk / legs.size();
			double startPoint = minStrike - (maxPrem * 2);
			double endPoint = maxStrike + (maxPrem * 2);

			xPoints.insertElementAt(startPoint, 0);
			xPoints.add(endPoint);
			
			for(int i = 0; i < legs.size(); i++){
				StrategyLeg st = legs.get(i);
				double p = st.getPremium();
				
				XYSeries series = new XYSeries("Leg"+st.getLegId());
				if(StrategyLeg.LONG.equals(st.getLs())){
					p = p * -1;  // paid
				}else{
					maxPrem = Math.abs(maxPrem) * -1;  // paid
				}
				double x,y;
				if(st.getCallOrPut().equals(StrategyLeg.CALL)){
					x = startPoint;
					y = p;
					series.add(x, y);
					x = st.getStrike();
					series.add(st.getStrike(), p);
					x = st.getStrike()+(Math.abs(maxPrem * 2)+Math.abs(p));
					y = maxPrem * 2;
					series.add(x, y);
				}else{
					x = startPoint;
					y = maxPrem * 2;
					series.add(x, y);
					x = st.getStrike();
					y = p;
					series.add(x, y);
					x = endPoint;
					series.add(x, y);
				}
				dataset.addSeries(series);
				
				st.addDataUpdateHandler(this);
			}
			XYSeries xynet = new XYSeries("Net");
			for(double price : xPoints){
				double profit = 0;
				for(StrategyLeg st : legs){
					profit += st.getProfit(price);
				}
				xynet.add(price, profit);
			}
			
			dataset.addSeries(xynet);
		}
	}

	private void setSummaryText(Quote rfq){
		// Text
		if(rfq == null
				|| Quote.STATUS_NEW.equals(rfq.getStatus())
				|| Quote.STATUS_REJECT.equals(rfq.getStatus())){
			text.setText("");
			return;
		}
		StringBuffer sbSt = new StringBuffer();
		if(Quote.SELL.equals(rfq.getBuyOrSell())){
			
		}
		
		double netPremium = 0;
		double netDelta   = 0;
		List<StrategyLeg> legs = rfq.getStrategies();
		for(int i = 0; i < legs.size(); i++){
			StrategyLeg st = legs.get(i);
			
			double qty = st.getQuantity();
			boolean lng = StrategyLeg.LONG.equals(st.getLs());
			double p = st.getPremium() * qty * (lng ? -1 : 1);
			double d = st.getDelta() * qty * (lng ? -1 : 1);
			
			netPremium += p;
			netDelta   += d;
			
			sbSt.append("Leg-").append(st.getLegId()).append(" ");
			sbSt.append(formatPremium.format(p)).append(" @ ").append(formatPremium.format(st.getPremium())).append(" x ").append(qty);
			sbSt.append("   Delta: ").append(formatDelta.format(d)).append(" @ " ).append(formatDelta.format(st.getDelta())).append(" * ").append(qty);
			sbSt.append("\n");
		}
		StringBuffer sb = new StringBuffer();
		sb.append("Net Premium: ").append(formatPremium.format(netPremium)).append("\n");
		sb.append("Net Delta:   ").append(formatDelta.format(netDelta)).append("\n");
		sb.append("----------------------------------\n");
		sb.append(sbSt);
		text.setText(sb.toString());
	}
	
	public void update(DataUpdateEvent event){
		setRFQ(rfq);
	}
	
	public static void  main(String[] args){
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Quote quote = new QuoteImpl(){};
		quote.setBuyOrSell(Quote.BUY);
		
		StrategyLeg leg = new StrategyLeg(quote);
		quote.addStrategy(leg);
		leg.setCallOrPut(StrategyLeg.PUT);
		leg.setPremium(10);
		leg.setStrike(100);
		leg.setLs(StrategyLeg.LONG);

		leg = new StrategyLeg(quote);
		quote.addStrategy(leg);
		leg.setCallOrPut(StrategyLeg.PUT);
		leg.setPremium(8);
		leg.setStrike(105);
		leg.setLs(StrategyLeg.SHORT);
		
		SummaryPanel panel = new SummaryPanel();
		panel.setRFQ(quote);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
}