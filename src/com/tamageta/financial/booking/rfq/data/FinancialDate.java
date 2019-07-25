package com.tamageta.financial.booking.rfq.data;

import java.util.Date;
import java.util.TimeZone;

public class FinancialDate extends Date {
	private TimeZone timeZone = TimeZone.getDefault();
	
	public FinancialDate(TimeZone timeZone){
		this.timeZone = timeZone;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
}
