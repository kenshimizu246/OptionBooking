package com.tamageta.financial.booking.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.tamageta.financial.util.HolidayHelper;

public class CalendarPanel extends JPanel {
	private static String[] weeks = new String[]{
		"Sun","Mon","Tue","Wed","Thu","Fri","Sat"
	};
	private static String[] months = new String[]{
		"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
	};
	
	private Color holidayColor = Color.ORANGE;
	private Color saturdayColor = Color.BLUE;
	private Color sundayColor = Color.RED;
	private Color defaultColor = Color.BLACK;

	private Date selectedDate = null;
	private final JComboBox comboYear = new JComboBox();
	private final JComboBox comboMonth = new JComboBox(months);
	private final JToggleButton[] days = new JToggleButton[42];
	private Calendar cal = GregorianCalendar.getInstance();
	private HolidayHelper holidayHelper = null;
	private Vector<CalendarPanelListener> itemListeners = new Vector<CalendarPanelListener>();
	
	public CalendarPanel(){
		JPanel calendar = new JPanel();
		JPanel control  = new JPanel();

		control.add(comboYear);
		control.add(comboMonth);
		
		setLayout(new BorderLayout());
		add(calendar, BorderLayout.CENTER);
		add(control, BorderLayout.NORTH);
		
		cal.add(Calendar.YEAR,-9);
		for(int i = 0; i < 20; i++){
			comboYear.addItem(new Integer(cal.get(Calendar.YEAR)));
			cal.add(Calendar.YEAR, 1);
		}
		
		cal.setTime(new Date());
		comboYear.setSelectedItem(new Integer(cal.get(Calendar.YEAR)));
		comboMonth.setSelectedItem(months[cal.get(Calendar.MONTH)]);
		
		calendar.setLayout(new GridLayout(7,7));
		for(String wl : weeks){
			calendar.add(new JLabel(wl));
		}
		
		ActionListener ll = new ActionListener(){
			public void actionPerformed(final ActionEvent event) {
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						for(JToggleButton b : days){
							if(!event.getSource().equals(b)){
								b.setSelected(false);
							}else{
								fireDaySelectionChanged(new CalendarPanelEvent(event.getSource(), getDate()));
							}
						}
					}
				});
			}
		};
		for(int i = 0; i < days.length; i++){
			days[i] = new JToggleButton();
			days[i].addActionListener(ll);
			calendar.add(days[i]);
		}
		comboYear.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				SwingUtilities.invokeLater(new Runnable(){
					public  void run(){
						setDate((Integer)comboYear.getSelectedItem(), 
								comboMonth.getSelectedIndex()+1);
					}
				});
			}
		});
		comboMonth.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				SwingUtilities.invokeLater(new Runnable(){
					public  void run(){
						setDate((Integer)comboYear.getSelectedItem(), 
								comboMonth.getSelectedIndex()+1);
					}
				});
			}
		});
		setDate(new Date());
	}
	
	public void setDate(Date date){
		cal.setTime(date);
		setDate(cal.get(Calendar.YEAR)
				, cal.get(Calendar.MONTH)+1
				, cal.get(Calendar.DAY_OF_MONTH));
		selectedDate = date;
	}
	
	public void setDate(int year, int month){
		setDate(year,month,-1);
		fireDaySelectionChanged(new CalendarPanelEvent(this, null));
	}
	
	public void setDate(int year, int month, int day){
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		int start = cal.get(Calendar.DAY_OF_WEEK);
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		int end = cal.get(Calendar.DAY_OF_MONTH);

		for(int i = 0; i < days.length; i++){
			int dd = (i+1) - start +1;

			if(0 < dd && dd <= end){
				days[i].setText(Integer.toString(dd));
				days[i].setVisible(true);
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, month-1);
				cal.set(Calendar.DAY_OF_MONTH, dd);
				if(cal.get(Calendar.DAY_OF_WEEK) == 1){
					days[i].setForeground(sundayColor);
				}else if(cal.get(Calendar.DAY_OF_WEEK) == 7){
					days[i].setForeground(saturdayColor);
				}else if(holidayHelper != null 
						&& holidayHelper.isHoliday(cal.getTime())){
					days[i].setForeground(holidayColor);
					days[i].setToolTipText(holidayHelper.getHolidayDescription(cal.getTime()));
				}else{
					days[i].setForeground(defaultColor);
				}
				days[i].setSelected((0 < day && day == dd));
			}else{
				days[i].setText("");
				days[i].setVisible(false);
				days[i].setSelected(false);
			}
		}
		fireDaySelectionChanged(new CalendarPanelEvent(this, getDate()));
	}
	
	public Date getDate(){
		cal.set(Calendar.YEAR, (Integer)comboYear.getSelectedItem());
		cal.set(Calendar.MONTH, comboMonth.getSelectedIndex()+1);
		for(JToggleButton b: days){
			if(!b.isSelected()){
				continue;
			}
			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(b.getText()));
			return cal.getTime();
		}
		return null;
	}

	public Color getHolidayColor() {
		return holidayColor;
	}

	public void setHolidayColor(Color holidayColor) {
		this.holidayColor = holidayColor;
	}

	public Color getSaturdayColor() {
		return saturdayColor;
	}

	public void setSaturdayColor(Color saturdayColor) {
		this.saturdayColor = saturdayColor;
	}

	public Color getSundayColor() {
		return sundayColor;
	}

	public void setSundayColor(Color sundayColor) {
		this.sundayColor = sundayColor;
	}

	public Color getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(Color defaultColor) {
		this.defaultColor = defaultColor;
	}

	public HolidayHelper getHolidayHelper() {
		return holidayHelper;
	}

	public void setHolidayHelper(HolidayHelper holidayHelper) {
		this.holidayHelper = holidayHelper;
	}
	
	private void fireDaySelectionChanged(CalendarPanelEvent event){
		for(CalendarPanelListener l : itemListeners){
			l.daySelectionChanged(event);
		}
	}
	
	public void addItemListner(CalendarPanelListener listener){
		itemListeners.add(listener);
	}
	
	public void removeItemListener(CalendarPanelListener listener){
		itemListeners.remove(listener);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		CalendarPanel cal = new CalendarPanel();
		cal.setDate(2008, 1);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(cal, BorderLayout.CENTER);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

}
