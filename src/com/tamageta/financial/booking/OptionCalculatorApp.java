package com.tamageta.financial.booking;

import java.awt.Dimension;

import javax.swing.JFrame;

import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.QuoteImpl;

public class OptionCalculatorApp {
	public OptionCalculatorApp(String[] args) {
		Quote rfq = new QuoteImpl(){};
		rfq.setRiskFreeRate(0.001d);
		rfq.setSpotPrice(100d);
		rfq.setStatus(Quote.STATUS_ANALYSIS);
		
		OptionCalculatorFrame frame = new OptionCalculatorFrame();
		frame.setRFQ(rfq);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		Dimension d = frame.getPreferredSize();
		d.setSize(800, 580);
		frame.setPreferredSize(d);
		frame.setSize(d);
		frame.setVisible(true);
	}
	public static void main(String[] args) {
		OptionCalculatorApp analyzer = new OptionCalculatorApp(args);
	}
}
