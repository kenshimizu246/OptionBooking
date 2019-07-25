package com.tamageta.financial.util;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SimpleDayCalculator extends JPanel {
	private final JButton btnCalc = new JButton("Calc");
	private final JFormattedTextField textStartDate = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
	private final JFormattedTextField textEndDate   = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
	private final JFormattedTextField textDays      = new JFormattedTextField(new DecimalFormat("##0.0"));
	
	public SimpleDayCalculator(){
		textStartDate.setValue(new Date());
		textEndDate.setValue(new Date());
		setLayout(new GridBagLayout());
		add(new JLabel("Start:"), 0,0,1);
		add(textStartDate, 0, 1, 1);
		add(new JLabel("End:"),   1,0,1);
		add(textEndDate, 1, 1, 1);
		add(new JLabel("Days:"),2,0,1);
		add(textDays,2,1,1);
		
		textEndDate.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e){}
			public void focusLost(FocusEvent e) {
				try{
					textEndDate.commitEdit();
					calcDays();
				}catch(ParseException ex){
					ex.printStackTrace();
				}
			}
		});
	}
	
	public void add(JComponent cmp, int y, int x, int width){
		GridBagConstraints c = new GridBagConstraints();
		 c.gridheight = 1;  
		 c.gridwidth  = 1;  
		 c.gridy = y;    
		 c.gridx = x;  
		 c.weightx = 0.0;  
		 c.insets = new Insets(5, 5, 5, 0);  
		 c.anchor = GridBagConstraints.WEST;
		 c.fill = GridBagConstraints.HORIZONTAL;
		 add(cmp, c);
	}
	
	public void calcDays(){
		Date start = (Date)textStartDate.getValue();
		Date end   = (Date)textEndDate.getValue();
		Calendar cal = new GregorianCalendar();
		cal.setTime(start);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long s = cal.getTime().getTime();
		cal.setTime(end);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long e = cal.getTime().getTime();
		
		System.out.println(e-s);
		textDays.setValue((e-s)/(24*60*60*1000));
	}
	
	public static void main(String[] args) {
		final SimpleDayCalculator panel = new SimpleDayCalculator();
		JButton btnCalc = new JButton("Calc");
		btnCalc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evnet) {
				panel.calcDays();
			}
		});
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.getContentPane().add(btnCalc, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

	}

}
