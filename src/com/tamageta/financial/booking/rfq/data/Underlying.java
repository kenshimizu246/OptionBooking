package com.tamageta.financial.booking.rfq.data;

import java.util.Date;

public class Underlying {
	private String quick = null;
	private String ric = null;
	private String mic = "XTKS";
	private String name = null;
	private int multiplier;
	private Date priceDate;
	private double priceOpen;
	private double priceClose;
	private double priceHigh;
	private double priceLow;
	private double volume;
	private double averageVolume;
	private double sharesOut;
	private double epsTtm;
	private double dividend;
	private double yield;
	private double divYield;
	private Date exDivDate;
	private double earnings;
	private double priceEarnings;
	private double relativePe;
	private double roe;
	private double lastDivRep;
	private double relDivYield;
	private double vol90;
	private double betaVsTpx; 
	
	private Date updateTime;
	
	public Underlying(String code, String name, String mic, int multipler){
		setRic(code);
		setName(name);
	}
	public Underlying(){
	}
	public String getQuick() {
		return quick;
	}
	public void setQuick(String quick) {
		this.quick = quick;
	}
	public String getRic() {
		return ric;
	}
	public void setRic(String ric) {
		this.ric = ric;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getPriceOpen() {
		return priceOpen;
	}
	public void setPriceOpen(double priceOpen) {
		this.priceOpen = priceOpen;
	}
	public double getPriceClose() {
		return priceClose;
	}
	public void setPriceClose(double priceClose) {
		this.priceClose = priceClose;
	}
	public double getPriceHigh() {
		return priceHigh;
	}
	public void setPriceHigh(double priceHigh) {
		this.priceHigh = priceHigh;
	}
	public double getPriceLow() {
		return priceLow;
	}
	public void setPriceLow(double priceLow) {
		this.priceLow = priceLow;
	}
	public double getVolume() {
		return volume;
	}
	public void setVolume(double volume) {
		this.volume = volume;
	}
	public double getAverageVolume() {
		return averageVolume;
	}
	public void setAverageVolume(double averageVolume) {
		this.averageVolume = averageVolume;
	}
	public double getSharesOut() {
		return sharesOut;
	}
	public void setSharesOut(double sharesOut) {
		this.sharesOut = sharesOut;
	}
	public double getEpsTtm() {
		return epsTtm;
	}
	public void setEpsTtm(double epsTtm) {
		this.epsTtm = epsTtm;
	}
	public double getDividend() {
		return dividend;
	}
	public void setDividend(double dividend) {
		this.dividend = dividend;
	}
	public double getYield() {
		return yield;
	}
	public void setYield(double yield) {
		this.yield = yield;
	}
	public Date getExDivDate() {
		return exDivDate;
	}
	public void setExDivDate(Date exDivDate) {
		this.exDivDate = exDivDate;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public String getMic() {
		return mic;
	}
	public void setMic(String mic) {
		this.mic = mic;
	}
	public Date getPriceDate() {
		return priceDate;
	}
	public void setPriceDate(Date priceDate) {
		this.priceDate = priceDate;
	}
	public int getMultiplier() {
		return multiplier;
	}
	public void setMultiplier(int multiplier) {
		this.multiplier = multiplier;
	}
	public double getDivYield() {
		return divYield;
	}
	public void setDivYield(double divYield) {
		this.divYield = divYield;
	}
	public double getEarnings() {
		return earnings;
	}
	public void setEarnings(double earnings) {
		this.earnings = earnings;
	}
	public double getPriceEarnings() {
		return priceEarnings;
	}
	public void setPriceEarnings(double priceEarnings) {
		this.priceEarnings = priceEarnings;
	}
	public double getRelativePe() {
		return relativePe;
	}
	public void setRelativePe(double relativePe) {
		this.relativePe = relativePe;
	}
	public double getRoe() {
		return roe;
	}
	public void setRoe(double roe) {
		this.roe = roe;
	}
	public double getLastDivRep() {
		return lastDivRep;
	}
	public void setLastDivRep(double lastDivRep) {
		this.lastDivRep = lastDivRep;
	}
	public double getRelDivYield() {
		return relDivYield;
	}
	public void setRelDivYield(double relDivYield) {
		this.relDivYield = relDivYield;
	}
	public double getVol90() {
		return vol90;
	}
	public void setVol90(double vol90) {
		this.vol90 = vol90;
	}
	public double getBetaVsTpx() {
		return betaVsTpx;
	}
	public void setBetaVsTpx(double betaVsTpx) {
		this.betaVsTpx = betaVsTpx;
	}
}
