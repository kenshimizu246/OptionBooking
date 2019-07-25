package com.tamageta.financial.booking.rfq.data;

public interface DataUpdateNotifiable {

	public abstract void addDataUpdateHandler(DataUpdateHandler handler);

	public abstract void removeDataHandler(DataUpdateHandler handler);

	public abstract void fireDataUpdateHandler(DataUpdateEvent event);

}