package com.tamageta.financial.booking.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.tamageta.financial.util.HolidayHelper;


public class DateField extends JFormattedTextField {
	private Calendar calendar = new GregorianCalendar();
	private HolidayHelper holidayHelper = null;
	private Date tradeDate = new Date();
	
	public DateField(){
		this( new SimpleDateFormat("yyyy-MM-dd"));
	}
	public DateField(DateFormat formatter){
		super(formatter);
		this.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent event) {
				System.out.println("f0");
			}
			public void focusLost(FocusEvent event) {
				System.out.println("f2");
				convertMacro();
			}
		});
		this.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent event) {
				System.out.println("t0");
				convertMacro();
			}
			public void keyReleased(KeyEvent event) {
				System.out.println("t1");
				convertMacro();
			}
			public void keyTyped(KeyEvent event) {
				System.out.println("t2");
				convertMacro();
			}
		});
		this.addInputMethodListener(new InputMethodListener(){
			public void caretPositionChanged(InputMethodEvent arg0) {
				System.out.println("i1");
			}
			public void inputMethodTextChanged(InputMethodEvent arg0) {
				System.out.println("i2");
			}
			
		});
	}
	public void convertMacro(){
		System.out.println("0");
		String s = getText();
		System.out.println("1");
		if(s == null || s.trim().length() < 1){
			System.out.println("2");
			return;
		}
		System.out.println("3");
		s = s.trim().toLowerCase();
		System.out.println("4");
		if(s.matches("^\\d+d$")){
			calendar.setTime(tradeDate);
			calendar.add(Calendar.DAY_OF_YEAR, 
					Integer.parseInt(s.substring(0, s.length()-1)));
			while(calendar.get(Calendar.DAY_OF_WEEK) == 1
					|| calendar.get(Calendar.DAY_OF_WEEK) == 7
					|| (holidayHelper != null 
							&& holidayHelper.isHoliday(calendar.getTime()))){
				calendar.add(Calendar.DAY_OF_YEAR, 1);
			}
			//setText(formatter.format(calendar.getTime()));
			setValue(calendar.getTime());
			System.out.println("5");
		}else if(s.matches("^\\d+w$")){
			calendar.setTime(tradeDate);
			calendar.add(Calendar.WEEK_OF_YEAR, 
					Integer.parseInt(s.substring(0, s.length()-1)));
			while(calendar.get(Calendar.DAY_OF_WEEK) == 1
					|| calendar.get(Calendar.DAY_OF_WEEK) == 7
					|| (holidayHelper != null 
							&& holidayHelper.isHoliday(calendar.getTime()))){
				calendar.add(Calendar.DAY_OF_YEAR, 1);
			}
			setValue(calendar.getTime());
			//setText(formatter.format(calendar.getTime()));
			System.out.println("6");
		}else if(s.matches("^\\d+m$")){
			calendar.setTime(tradeDate);
			calendar.add(Calendar.MONTH, 
					Integer.parseInt(s.substring(0, s.length()-1)));
			while(calendar.get(Calendar.DAY_OF_WEEK) == 1
					|| calendar.get(Calendar.DAY_OF_WEEK) == 7
					|| (holidayHelper != null 
							&& holidayHelper.isHoliday(calendar.getTime()))){
				calendar.add(Calendar.DAY_OF_YEAR, 1);
			}
			setValue(calendar.getTime());
			//setText(formatter.format(calendar.getTime()));
			System.out.println("6");
		}else if(s.matches("^\\d+y$")){
			calendar.setTime(tradeDate);
			calendar.add(Calendar.YEAR, 
					Integer.parseInt(s.substring(0, s.length()-1)));
			while(calendar.get(Calendar.DAY_OF_WEEK) == 1
					|| calendar.get(Calendar.DAY_OF_WEEK) == 7
					|| (holidayHelper != null 
							&& holidayHelper.isHoliday(calendar.getTime()))){
				calendar.add(Calendar.DAY_OF_YEAR, 1);
			}
			setValue(calendar.getTime());
			//setText(formatter.format(calendar.getTime()));
			System.out.println("7");
		}else if(s.matches("^t\\+\\d+$") || s.matches("^\\d+t$") ){
			calendar.setTime(tradeDate);
			int days = (s.matches("^t\\+\\d+$")
					? Integer.parseInt(s.substring(2))
							: Integer.parseInt(s.substring(0, s.length()-1)));
			int cnt = 0;
			do{
				calendar.add(Calendar.DAY_OF_YEAR, 1);
				if(calendar.get(Calendar.DAY_OF_WEEK) == 1
						|| calendar.get(Calendar.DAY_OF_WEEK) == 7
						|| (holidayHelper != null 
								&& holidayHelper.isHoliday(calendar.getTime()))){
					continue;
				}
				cnt++;
			}while(cnt < days);
			//setText(formatter.format(calendar.getTime()));
			setValue(calendar.getTime());
			System.out.println("8");
		}
		System.out.println("9");
	}
	
//	public Object getValue(){
//		try {
//			convertMacro();
//			return formatter.parse(getText());
//		} catch (ParseException e) {
//			//e.printStackTrace();
//		}
//		return getText();
//	}
//	
//	public void setValue(Object value){
//		if(value == null){
//			setText("");
//		}else if(value instanceof Date){
//			setText(formatter.format((Date)value));
//		}else{
//			setText(value.toString());
//		}
//	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DateField dateField = new DateField();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(dateField);
		frame.pack();
		frame.setVisible(true);
	}
	public Date getTradeDate() {
		return tradeDate;
	}
	public void setTradeDate(Date tradeDate) {
		this.tradeDate = tradeDate;
	}
}
