package com.tamageta.financial.booking.rfq;

import java.util.Date;

public class Dividend {
	private Date date;
	private double dividend;
	private double spotValue = Double.NaN;
	
	public Dividend(){
		
	}
	public Dividend(Date date, double dividend){
		this.date = date;
		this.dividend = dividend;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public double getDividend() {
		return dividend;
	}
	public void setDividend(double dividend) {
		this.dividend = dividend;
	}
	public double getSpotValue() {
		return spotValue;
	}
	public void setSpotValue(double spotValue) {
		this.spotValue = spotValue;
	}
}
