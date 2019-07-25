package com.tamageta.financial.booking.rfq;

import java.util.EventObject;

import com.tamageta.financial.booking.rfq.data.Underlying;

public class UnderlyingEvent extends EventObject {
	private final Underlying underlying;
	public UnderlyingEvent(Object source, Underlying underlying) {
		super(source);
		this.underlying = underlying;
	}
	public Underlying getUnderlying() {
		return underlying;
	}
}
