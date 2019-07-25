package com.tamageta.financial.booking.rfq.data;

import java.util.EventObject;

public class DataUpdateEvent extends EventObject {
	private final String type;

	public DataUpdateEvent(Object source, String type) {
		super(source);
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
