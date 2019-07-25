package com.tamageta.financial.booking.gui;

import java.awt.GridLayout;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.tamageta.financial.booking.rfq.data.Underlying;

public class UnderlyingPanel extends JPanel {
	private Underlying underlying = null;
	/*
	private String quick = null;
	private String ric = null;
	private String mic = "XTKS";
	private String name = null;
	private int multiplier;
	private Date priceDate;
	private double priceOpen;
	private double priceClose;
	private double priceHigh;
	private double priceLow;
	private double volume;
	private double averageVolume;
	private double sharesOut;
	private double epsTtm;
	private double dividend;
	private double yield;
	private double divYield;
	private Date exDivDate;
	private double earnings;
	private double priceEarnings;
	private double relativePe;
	private double roe;
	private double lastDivRep;
	private double relDivYield;
	private double vol90;
	private double betaVsTpx; 
	
	private Date updateTime;
	 */
	public UnderlyingPanel(){
		JPanel panelProp = new JPanel();
		add(panelProp);
		panelProp.setLayout(new GridLayout(2,2));
		panelProp.add(new JLabel("Open:"));
		panelProp.add(new JLabel("Close:"));
		panelProp.add(new JLabel("c"));
		panelProp.add(new JLabel("d"));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UnderlyingPanel p = new UnderlyingPanel();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(p);
		frame.pack();
		frame.setVisible(true);
	}

	public Underlying getUnderlying() {
		return underlying;
	}

	public void setUnderlying(Underlying underlying) {
		this.underlying = underlying;
	}

}
