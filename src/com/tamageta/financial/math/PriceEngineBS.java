package com.tamageta.financial.math;

import java.io.Serializable;

import com.tamageta.financial.booking.rfq.data.StrategyLeg;

public class PriceEngineBS {
	private double tolerance = 0.0000000001;
	private int ivMaxCalc = 100000;
	
	private boolean call = true;
	private double spot = Double.NaN;
	private double strike = Double.NaN;
	private double volatility = Double.NaN;
	private double rate = Double.NaN;
	private double time = Double.NaN;

	private double d1 = Double.NaN;
	private double d2 = Double.NaN;
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
	
	private boolean byPremium = false;
	
	public PriceEngineBS(){
	}
	
	public PriceEngineBS(boolean call, double spot, double strike, double volatility, double rate, double time){
		this.call = call;
		this.spot = spot;
		this.strike = strike;
		this.volatility = volatility;
		this.rate = rate;
		this.time = time;
	}
	
	public PriceEngineBS(boolean call, double spot, double strike, double premium, double rate, double time, boolean reverse){
		this.call = call;
		this.spot = spot;
		this.strike = strike;
		this.premium = premium;
		this.rate = rate;
		this.time = time;
		this.byPremium = reverse;
	}
	
	public static void main(String[] args){
		boolean call = true;
		double spot = 500;
		double strike = 570;
		double volatility = 0.23;
		double rate = 0.005;
		double time = 1;
	
		PriceEngineBS p1 = new PriceEngineBS(call, spot, strike, volatility, rate, time);
		p1.calc();
		double premium = p1.getPremium();
		PriceEngineBS p2 = new PriceEngineBS(call, spot, strike, premium, rate, time, true);
		p2.calc();
		System.out.println("p1:"+p1.getPremium());
		System.out.println("p2:"+p2.getPremium());
		System.out.println("v1:"+p1.getVolatility());
		System.out.println("v2:"+p2.getVolatility());
		
		for(int i = 0; i > 100; i++){
			p1 = new PriceEngineBS(call, spot, strike, p2.getVolatility(), rate, time);
			p1.calc();
			p2 = new PriceEngineBS(call, spot, strike, p1.getPremium(), rate, time, true);
			p2.calc();
		}
		System.out.println("p1:"+p1.getPremium());
		System.out.println("p2:"+p2.getPremium());
		System.out.println("v1:"+p1.getVolatility());
		System.out.println("v2:"+p2.getVolatility());
		
		p1 = new PriceEngineBS();
		p1.setCall(call);
		p1.setSpot(spot);
		p1.setStrike(strike);
		p1.setVolatility(volatility);
		p1.setRate(rate);
		p1.setTime(time);
		p1.calc();

		p2 = new PriceEngineBS();
		p2.setCall(call);
		p2.setSpot(spot);
		p2.setStrike(strike);
		p2.setVolatility(Double.NaN);
		p2.setRate(rate);
		p2.setTime(time);
		p2.setPremium(p1.getPremium());
		p2.calc();
		System.out.println("p1:"+p1.getPremium());
		System.out.println("p2:"+p2.getPremium());
		System.out.println("v1:"+p1.getVolatility());
		System.out.println("v2:"+p2.getVolatility());
		
	}
	
	public double calcImpliedVolatility(double premium){
		double h = 1;
		double l = 0;
		
		for(int i = 0; i < ivMaxCalc; i++){
			double m = (h + l)/2;
			double c1 = l + (m - l)/2;
			double c2 = h - (h - m)/2;
			
			double p1 = getPremium(c1);
			double p2 = getPremium(c2);

			if(Math.abs(premium - p1) < tolerance){
				return c1;
			}else if(Math.abs(premium - p2) < tolerance){
				return c2;
			}else if(Math.abs(premium - p1) < Math.abs(premium - p2)){
				h = m;
			}else{
				l = m;
			}
		}
		return 0;
	}
	
	private double getPremium(double v){
		double d1 = (Math.log(spot/strike)+(rate + (Math.pow(v, 2d)/2d))*time)
			/(v*Math.sqrt(time));

		double d2 = d1 - v * Math.sqrt(time);

		if(call){
			return spot * Probability.normalCdf(d1)
			-(strike * (Math.pow(Math.E, -1d * rate * time)) * Probability.normalCdf(d2));
		}else{
			return strike * (Math.pow(Math.E, -1d * rate * time)) * Probability.normalCdf(d2*-1d)
			-(spot * Probability.normalCdf(d1*-1d));
		}
	}
	
	public void calc(){
		if(byPremium){
			volatility = calcImpliedVolatility(premium);
		}
		double d1 = (Math.log(spot/strike)+(rate + (Math.pow(volatility, 2d)/2d))*time)
			/(volatility*Math.sqrt(time));
		
		double d2 = d1 - volatility * Math.sqrt(time);
		
		double p1 = Probability.normalCdf(d1);

		if(call){
			if(!byPremium){
				premium = spot * Probability.normalCdf(d1)
						-(strike * (Math.pow(Math.E, -1d * rate * time)) * Probability.normalCdf(d2));
			}
			delta = Probability.normalCdf(d1); // PriceEngineUtil.deltaCall(spot, strike, volatility, rate, time);
			gamma = PriceEngineUtil.gamma(spot, strike, volatility, rate, time);
			vega  = PriceEngineUtil.vega(spot, strike, volatility, rate, time)/100d;
			theta = PriceEngineUtil.thetaCall(spot, strike, volatility, rate, time)/365d;
			rho   = PriceEngineUtil.rhoCall(spot, strike, volatility, rate, time)/100d;
			volga = vega * (d1*d2/volatility);
			vanna = (spot / vega * Math.abs(1 - d1 / (volatility * Math.sqrt(time))))/100d;
			dualDelta = Math.pow(Math.E, -1d * rate * time)
				      * Probability.normalCdf(d2);
			dualGamma = Math.pow(Math.E, -1d * rate * time)
				      * Math.pow(Math.E, -1 * Math.pow(d2, 2d)/2)/(Math.sqrt(2*Math.PI))
				      ;
		}else{
			if(!byPremium){
				premium = strike * (Math.pow(Math.E, -1d * rate * time)) * Probability.normalCdf(d2*-1d)
						-(spot * Probability.normalCdf(d1*-1d));
			}
			delta = -1 * Probability.normalCdf(d1 * -1); //PriceEngineUtil.deltaPut(spot, strike, volatility, rate, time);
			gamma = PriceEngineUtil.gamma(spot, strike, volatility, rate, time);
			vega  = PriceEngineUtil.vega(spot, strike, volatility, rate, time);
			theta = PriceEngineUtil.thetaPut(spot, strike, volatility, rate, time);
			rho   = PriceEngineUtil.rhoPut(spot, strike, volatility, rate, time);
			volga = vega * (d1*d2/volatility);
			vanna = (spot / vega * Math.abs(1 - d1 / (volatility * Math.sqrt(time))))/100d;
			dualDelta = Math.pow(Math.E, -1d * rate * time)
				      * Probability.normalCdf(d2 * -1d);
			dualGamma = Math.pow(Math.E, -1d * rate * time)
				      * Math.pow(Math.E, -1 * Math.pow(d2, 2d)/2)/(Math.sqrt(2*Math.PI))
				      ;
		}
		
	}
	
	private double normalCdf(double d){
		return Probability.normalCdf(d);
	}
	
	private double normalPdf(double d){
		return Math.pow(Math.E, -1 * Math.pow(d, 2d)/2)/(Math.sqrt(2*Math.PI));
	}
	
	public double getPremium(){
		return premium;
	}
	
	public void setPremium(double premium){
		this.premium = premium;
		if(Double.isNaN(premium)){
			this.byPremium = false;
		}else{
			this.byPremium = true;
			this.volatility = Double.NaN;
		}
	}

	public double getVolatility() {
		return volatility;
	}

	public void setVolatility(double volatility) {
		this.volatility = volatility;
		if(Double.isNaN(volatility)){
			this.byPremium = true;
		}else{
			this.byPremium = false;
			this.premium = Double.NaN;
		}
	}
	
	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public double getSpot() {
		return spot;
	}

	public void setSpot(double spot) {
		this.spot = spot;
	}

	public double getStrike() {
		return strike;
	}

	public void setStrike(double strike) {
		this.strike = strike;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public double getCharm() {
		return charm;
	}

	public double getColor() {
		return color;
	}

	public double getDelta() {
		return delta;
	}

	public double getDualDelta() {
		return dualDelta;
	}

	public double getDualGamma() {
		return dualGamma;
	}

	public double getGamma() {
		return gamma;
	}

	public double getRho() {
		return rho;
	}

	public double getTheta() {
		return theta;
	}

	public double getVanna() {
		return vanna;
	}

	public double getVega() {
		return vega;
	}

	public double getVolga() {
		return volga;
	}

	public boolean isCall() {
		return call;
	}

	public void setCall(boolean call) {
		this.call = call;
	}
}
