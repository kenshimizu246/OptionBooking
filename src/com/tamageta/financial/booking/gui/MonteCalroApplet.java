package com.tamageta.financial.booking.gui;

import java.applet.Applet;
import java.awt.BorderLayout;

import com.tamageta.financial.booking.rfq.MonteCalroPanel;

public class MonteCalroApplet extends Applet {
	public MonteCalroApplet(){
		setLayout(new BorderLayout());
		MonteCalroPanel panelmc = new MonteCalroPanel();
		panelmc.setVolatility(0.38);
		panelmc.setRate(0.06);
		panelmc.setSpot(14500);
		panelmc.setStrike(14000);
		panelmc.setCount(500000);
		add(panelmc,BorderLayout.CENTER);
	}
}
