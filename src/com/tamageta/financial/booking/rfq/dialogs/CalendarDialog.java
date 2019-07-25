package com.tamageta.financial.booking.rfq.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.tamageta.financial.booking.gui.CalendarPanel;
import com.tamageta.financial.booking.gui.CalendarPanelEvent;
import com.tamageta.financial.booking.gui.CalendarPanelListener;
import com.tamageta.financial.util.HolidayHelper;

public class CalendarDialog extends AbstractDialog {
	private CalendarPanel panelCalendar = new CalendarPanel();
	
	public CalendarDialog(Frame arg0, String arg1, boolean arg2){
		super(arg0, arg1, arg2);
		initGui();
	}
	
	private void initGui(){
		getContentPane().add(panelCalendar, BorderLayout.CENTER);
		setOkEnabled(false);
		panelCalendar.addItemListner(new CalendarPanelListener(){
			public void daySelectionChanged(CalendarPanelEvent event) {
				if(event.getDate() != null){
					setOkEnabled(true);
				}else{
					setOkEnabled(false);
				}
			}
		});
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		CalendarDialog dialog = new CalendarDialog(frame, "", true);
		dialog.pack();
		dialog.setVisible(true);
	}

	public Date getDate() {
		return panelCalendar.getDate();
	}

	public void setDate(Date date) {
		panelCalendar.setDate(date);
	}

	public void setDefaultColor(Color defaultColor) {
		panelCalendar.setDefaultColor(defaultColor);
	}

	public void setHolidayColor(Color holidayColor) {
		panelCalendar.setHolidayColor(holidayColor);
	}

	public void setHolidayHelper(HolidayHelper holidayHelper) {
		panelCalendar.setHolidayHelper(holidayHelper);
	}

	public void setSaturdayColor(Color saturdayColor) {
		panelCalendar.setSaturdayColor(saturdayColor);
	}

	public void setSundayColor(Color sundayColor) {
		panelCalendar.setSundayColor(sundayColor);
	}

}
