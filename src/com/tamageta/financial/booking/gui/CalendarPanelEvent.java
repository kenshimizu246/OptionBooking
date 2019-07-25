package com.tamageta.financial.booking.gui;

import java.util.Date;

public class CalendarPanelEvent {
	private final Object source;
	private final Date date;
	
	public CalendarPanelEvent(Object source, Date date){
		this.source = source;
		this.date   = date;
	}

	public Object getSource() {
		return source;
	}

	public Date getDate() {
		return date;
	}
}
