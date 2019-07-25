package com.tamageta.financial.booking.rfq.data;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

import com.tamageta.financial.math.PriceEngineBS;

public class StrategyLeg extends DataUpdateNotifiableImpl implements Serializable{
	public static final String CALL = "CALL";
	public static final String PUT  = "PUT";

	public static final String LONG  = "L";
	public static final String SHORT = "S";
	
	private long id;
	private int legId;
	private String ls = LONG;
	private String callOrPut = CALL;
	private double quantity = Double.NaN;
	private double strike = Double.NaN;
	private Date expiry = null;
	private int multi = 1;
	private final Quote quote;
	private boolean byPremium = false;
	
	private double dateBasis = 365d;
	private double volatility = Double.NaN;
	private double premium = Double.NaN;
	private double delta = Double.NaN;
	private double gamma = Double.NaN;
	private double vega = Double.NaN;
	private double theta = Double.NaN;
	private double rho = Double.NaN;
	private double volga = Double.NaN;
	private double vanna = Double.NaN;
	private double charm = Double.NaN;
	private double color = Double.NaN;
	private double dualDelta = Double.NaN;
	private double dualGamma = Double.NaN;
	
	private transient PriceEngineBS pricer = new PriceEngineBS();
	
	public StrategyLeg(Quote quote){
		this.quote = quote;
	}
	public void doCalc(){
		if(quote.getSpotPrice() == Double.NaN || quote.getSpotPrice() < 0){
			return;
		}
		if(getStrike() == Double.NaN || getStrike() < 0){
			return;
		}
		if(quote.getRiskFreeRate() == Double.NaN || quote.getRiskFreeRate() < 0){
			return;
		}
		if(quote.getTradeDate() == null){
			return;
		}
		if(pricer == null){
			pricer = new PriceEngineBS();
		}
		long days = (getExpiry().getTime() - quote.getTradeDate().getTime())/1000/60/60/24;
		if(!byPremium){
			if(getVolatility() == Double.NaN){
				return;
			}
			pricer.setCall(StrategyLeg.CALL.equals(getCallOrPut()));
			pricer.setSpot(quote.getSpotPrice());
			pricer.setStrike(getStrike());
			pricer.setVolatility(getVolatility());
			pricer.setRate(quote.getRiskFreeRate());
			pricer.setTime(days/365d);
			pricer.calc();
		}else{
			if(getPremium() == Double.NaN){
				return;
			}
			pricer.setCall(StrategyLeg.CALL.equals(getCallOrPut()));
			pricer.setSpot(quote.getSpotPrice());
			pricer.setStrike(getStrike());
			pricer.setVolatility(Double.NaN);
			pricer.setRate(quote.getRiskFreeRate());
			pricer.setTime(days/365d);
			pricer.setPremium(getPremium());
			pricer.calc();
		}
		setVolatility(pricer.getVolatility(), false);
		setPremium(pricer.getPremium(), false);
		setDelta(pricer.getDelta(), false);
		setGamma(pricer.getGamma(), false);
		setVega(pricer.getVega(), false);
		setTheta(pricer.getTheta(), false);
		setRho(pricer.getRho(), false);
		setVolga(pricer.getVolga(), false);
		setVanna(pricer.getVanna(), false);
		setCharm(pricer.getCharm(), false);
		setColor(pricer.getColor(), false);
		setDualDelta(pricer.getDualDelta(), false);
		setDualGamma(pricer.getDualGamma(), false);
		fireDataUpdateHandler(new DataUpdateEvent(this, "doCalc"));
	}
	
	public void initCalc(){
		setVolatility(Double.NaN, false);
		setPremium(Double.NaN, false);
		setDelta(Double.NaN, false);
		setGamma(Double.NaN, false);
		setVega(Double.NaN, false);
		setTheta(Double.NaN, false);
		setRho(Double.NaN, false);
		setVolga(Double.NaN, false);
		setVanna(Double.NaN, false);
		setCharm(Double.NaN, false);
		setColor(Double.NaN, false);
		setDualDelta(Double.NaN, false);
		setDualGamma(Double.NaN, false);
		fireDataUpdateHandler(new DataUpdateEvent(this, "initCalc"));
	}
	
	public void setPriceEngine(PriceEngineBS pricer){
		this.pricer = pricer;
		fireDataUpdateHandler(new DataUpdateEvent(this, "pricer"));
	}
	public Quote getQuote(){
		return quote;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
		fireDataUpdateHandler(new DataUpdateEvent(this, ""));
	}
	public int getLegId() {
		return legId;
	}
	public void setLegId(int legId) {
		this.legId = legId;
		fireDataUpdateHandler(new DataUpdateEvent(this, ""));
	}

	public Date getExpiry() {
		return expiry;
	}
	public void setExpiry(Date expiry) {
		setExpiry(expiry, true);
	}
	private void setExpiry(Date expiry, boolean changeEvnet) {
		this.expiry = expiry;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "expiry"));
		}
	}
	public String getLs() {
		return ls;
	}
	public void setLs(String ls) {
		setLs(ls, true);
	}
	private void setLs(String ls, boolean changeEvnet) {
		this.ls = ls;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "ls"));
		}
	}
	public int getMulti() {
		return multi;
	}
	public void setMulti(int multi) {
		setMulti(multi, true);
	}
	private void setMulti(int multi, boolean changeEvnet) {
		this.multi = multi;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "multi"));
		}
	}
	public double getQuantity() {
		return quantity;
	}
	public void setQuantity(double qty) {
		setQuantity(qty, true);
	}
	private void setQuantity(double qty, boolean changeEvnet) {
		this.quantity = qty;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "quantity"));
		}
	}
	public double getStrike() {
		return strike;
	}
	public void setStrike(double strike) {
		setStrike(strike, true);
	}
	private void setStrike(double strike, boolean changeEvnet) {
		this.strike = strike;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "strike"));
		}
	}
	public String getCallOrPut() {
		return callOrPut;
	}
	public void setCallOrPut(String callOrPut) {
		setCallOrPut(callOrPut, true);
	}
	private void setCallOrPut(String callOrPut, boolean changeEvnet) {
		this.callOrPut = callOrPut;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "callOrPut"));
		}
	}
	public double getCharm() {
		return charm;
	}
	public void setCharm(double charm) {
		setCharm(charm, true);
	}
	public void setCharm(double charm, boolean changeEvnet) {
		this.charm = charm;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "charm"));
		}
	}
	public double getColor() {
		return color;
	}
	public void setColor(double color) {
		setColor(color, true);
	}
	private void setColor(double color, boolean changeEvnet) {
		this.color = color;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "color"));
		}
	}
	public double getDateBasis() {
		return dateBasis;
	}
	public void setDateBasis(double dateBasis) {
		setDateBasis(dateBasis, true);
	}
	private void setDateBasis(double dateBasis, boolean changeEvnet) {
		this.dateBasis = dateBasis;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "dateBasis"));
		}
	}
	public double getDelta() {
		return delta;
	}
	public void setDelta(double delta) {
		setDelta(delta, true);
	}
	private void setDelta(double delta, boolean changeEvnet) {
		this.delta = delta;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "delta"));
		}
	}
	public double getDualDelta() {
		return dualDelta;
	}
	public void setDualDelta(double dualDelta) {
		setDualDelta(dualDelta, true);
	}
	private void setDualDelta(double dualDelta, boolean changeEvnet) {
		this.dualDelta = dualDelta;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "dualDelta"));
		}
	}
	public double getDualGamma() {
		return dualGamma;
	}
	public void setDualGamma(double dualGamma) {
		setDualGamma(dualGamma, true);
	}
	private void setDualGamma(double dualGamma, boolean changeEvnet) {
		this.dualGamma = dualGamma;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "dualGamma"));
		}
	}
	public double getGamma() {
		return gamma;
	}
	public void setGamma(double gamma) {
		setGamma(gamma, true);
	}
	private void setGamma(double gamma, boolean changeEvnet) {
		this.gamma = gamma;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "gamma"));
		}
	}
	public double getPremium() {
		return premium;
	}
	public void setPremium(double premium) {
		setPremium(premium, true);
	}
	private void setPremium(double premium, boolean changeEvnet) {
		this.premium = premium;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "premium"));
		}
	}
	public double getRho() {
		return rho;
	}
	public void setRho(double rho) {
		setRho(rho, true);
	}
	private void setRho(double rho, boolean changeEvnet) {
		this.rho = rho;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "rho"));
		}
	}
	public double getTheta() {
		return theta;
	}
	public void setTheta(double theta) {
		setTheta(theta, true);
	}
	private void setTheta(double theta, boolean changeEvnet) {
		this.theta = theta;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "theta"));
		}
	}
	public double getVanna() {
		return vanna;
	}
	public void setVanna(double vanna) {
		setVanna(vanna, true);
	}
	private void setVanna(double vanna, boolean changeEvnet) {
		this.vanna = vanna;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "vanna"));
		}
	}
	public double getVega() {
		return vega;
	}
	public void setVega(double vega) {
		setVega(vega, true);
	}
	private void setVega(double vega, boolean changeEvnet) {
		this.vega = vega;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "vega"));
		}
	}
	public double getVolatility() {
		return volatility;
	}
	public void setVolatility(double volatility) {
		setVolatility(volatility, true);
	}
	public void setVolatility(double volatility, boolean changeEvnet) {
		this.volatility = volatility;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "volatility"));
		}
	}
	public double getVolga() {
		return volga;
	}
	public void setVolga(double volga) {
		setVolga(volga, true);
	}
	public void setVolga(double volga, boolean changeEvnet) {
		this.volga = volga;
		if(changeEvnet){
			fireDataUpdateHandler(new DataUpdateEvent(this, "volga"));
		}
	}
	public double getProfit(double price){
		double pp = 0;
		if(callOrPut == CALL){
			if(price < strike){ // out the money
				pp = premium * (ls == LONG?-1:1);
			}else if(price == strike){ // at the money
				pp = premium * (ls == LONG?-1:1);
			}else{ // in the money
				pp = (price - strike - premium) * (ls == LONG?-1:1);
			}
		}else{
			if(price < strike){ // in the money
				pp = (strike - price - premium) * (ls == LONG?-1:1);
			}else if(price == strike){ // at the money
				pp = premium * (ls == LONG?-1:1);
			}else{ // out the money
				pp = premium * (ls == LONG?-1:1);
			}
		}
		return pp;// * quantity;
	}

	public boolean isByPremium() {
		return byPremium;
	}

	public void setByPremium(boolean byPremium) {
		this.byPremium = byPremium;
	}
	
}
