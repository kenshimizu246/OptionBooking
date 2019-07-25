package com.tamageta.financial.booking.rfq;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.StrategyLeg;

public class PricePanel extends JPanel {
	Quote rfq;
	StrategyLeg leg;
	JSplitPane splitA = new JSplitPane();
	JScrollPane scrollDividend = new JScrollPane();
	DividendTableModel model = new DividendTableModel();
	JTable tableDividend = new JTable(model);
	
	
	public PricePanel(Quote rfq, StrategyLeg leg){
		this.rfq = rfq;
		this.leg = leg;
		
		setLayout(new BorderLayout());
		add(splitA,BorderLayout.CENTER);

		splitA.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		//splitA.add(scrollLegs, JSplitPane.TOP);
		splitA.add(scrollDividend, JSplitPane.BOTTOM);
		scrollDividend.getViewport().add(tableDividend);
	}
	
	public static void main(String[] args){
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.getContentPane().add(arg0)
	}
}
