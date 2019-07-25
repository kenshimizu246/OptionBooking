package com.tamageta.financial.booking.rfq.data;

import java.util.Date;

public class Price {
	private String ric;
	private Date price_timestamp;
	private double open;
	private double close;
	private double high;
	private double low;
	private double volume;
	private double div_yield;
	private double yield;
	
	public String getRic() {
		return ric;
	}
	public void setRic(String ric) {
		this.ric = ric;
	}
	public Date getPrice_timestamp() {
		return price_timestamp;
	}
	public void setPrice_timestamp(Date price_timestamp) {
		this.price_timestamp = price_timestamp;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getVolume() {
		return volume;
	}
	public void setVolume(double volume) {
		this.volume = volume;
	}
	public double getDiv_yield() {
		return div_yield;
	}
	public void setDiv_yield(double div_yield) {
		this.div_yield = div_yield;
	}
	public double getYield() {
		return yield;
	}
	public void setYield(double yield) {
		this.yield = yield;
	}
}
