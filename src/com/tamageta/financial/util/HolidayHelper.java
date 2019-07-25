package com.tamageta.financial.util;

import java.util.Date;

public interface HolidayHelper {
	public abstract boolean isHoliday(Date date);
	public abstract String getHolidayDescription(Date date);
}
