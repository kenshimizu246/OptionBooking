package com.tamageta.financial.booking.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class DecimalField extends JFormattedTextField {
	public DecimalField(){
		this(new DecimalFormat("###,###,###,###,###.#########"));
	}
	public DecimalField(DecimalFormat formatter){
		super(formatter);
		this.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent event) {
			}
			public void focusLost(FocusEvent event) {
				convertMacro();
			}
		});
		this.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent event) {
				convertMacro();
			}
			public void keyReleased(KeyEvent event) {
				convertMacro();
			}
			public void keyTyped(KeyEvent event) {
				convertMacro();
			}
		});
	}
	
	private void convertMacro(){
		String s = getText();
		s = s.trim().toLowerCase();
		if(s.matches("^\\d+\\.*\\d*m$")){
			double dd = Double.parseDouble(s.substring(0, s.length()-1));
			dd *= 1000000;
			setValue(dd);
			//setText(formatter.format(dd));
		}else if(s.matches("^\\d+\\.*\\d*b$")){
			double dd = Double.parseDouble(s.substring(0, s.length()-1));
			dd *= 1000000000;
			setValue(dd);
			//setText(formatter.format(dd));
		}
	}
	public static void main(String[] args) {
		DecimalField dateField = new DecimalField();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(dateField);
		frame.pack();
		frame.setVisible(true);
	}

}
