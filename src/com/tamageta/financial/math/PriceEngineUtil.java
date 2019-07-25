package com.tamageta.financial.math;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class PriceEngineUtil {
	public static final double BASIS365 = 365d;
	
	public static double calcD1(double spot, double strike, double volatility, double rate, double time){
		return (Math.log(spot/strike)+(rate + (Math.pow(volatility, 2d)/2d))*time)
							/(volatility*Math.sqrt(time));
	}
	
	public static double calcD2(double spot, double strike, double volatility, double rate, double time){
		return calcD1(spot, strike, volatility, rate, time) - volatility * Math.sqrt(time);
	}
	
	public static double calcProbDensity(double d){
		return Math.pow(Math.E, -1 * Math.pow(d, 2d)/2)/(Math.sqrt(2*Math.PI));
	}
	
	public static double calcCumDist(double d){
		return Probability.normalCdf(d);
	}
	
	public static double calcBsCall(double spot, double strike, double volatility, double rate, double time){
		return spot * calcCumDist(calcD1(spot, strike, volatility, rate, time))
				-(strike * (Math.pow(Math.E, -1d * rate * time)) * calcCumDist(calcD2(spot,strike,volatility,rate,time)));
	}
	
	public static double calcBsPut(double spot, double strike, double volatility, double rate, double time){
		return strike * (Math.pow(Math.E, -1d * rate * time)) * calcCumDist(calcD2(spot, strike, volatility, rate, time))
				-(spot * calcCumDist(calcD1(spot,strike,volatility,rate,time))*-1d);
	}
	
	public static double deltaCall(double spot, double strike, double volatility, double rate, double time){
		return calcCumDist(calcD1(spot, strike, volatility, rate, time));
	}
	
	public static double deltaPut(double spot, double strike, double volatility, double rate, double time){
		return -1 * calcCumDist(-1 * calcD1(spot, strike, volatility, rate, time));
	}
	
	public static double gamma(double spot, double strike, double volatility, double rate, double time){
		return calcProbDensity(calcD1(spot, strike, volatility, rate, time))
					/ (spot * volatility * Math.sqrt(time));
	}
	
	public static double vega(double spot, double strike, double volatility, double rate, double time){
		return spot * calcProbDensity(calcD1(spot, strike, volatility, rate, time))*Math.sqrt(time);
	}
	
	public static double thetaCall(double spot, double strike, double volatility, double rate, double time){
		return (-1 * (spot * calcProbDensity(calcD1(spot, strike, volatility, rate, time)) * volatility) / (2 * Math.sqrt(time)))
			- (rate * strike * Math.pow(Math.E, -1 * rate * time) * calcCumDist(calcD2(spot, strike, volatility, rate, time)));
	}
	
	public static double thetaPut(double spot, double strike, double volatility, double rate, double time){
		return ((spot * calcProbDensity(calcD1(spot, strike, volatility, rate, time)) * volatility) 
						/ (2 * Math.sqrt(time)))
				+ (rate * strike * Math.pow(Math.E, -1 * rate * time) 
						* calcCumDist(-1 * calcD2(spot, strike, volatility, rate, time)));
	}
	
	public static double rhoCall(double spot, double strike, double volatility, double rate, double time){
		return strike 
				* time 
				* Math.pow(Math.E, -1 * rate * time) 
				* calcCumDist(calcD2(spot, strike, volatility, rate, time));
	}
	
	public static double rhoPut(double spot, double strike, double volatility, double rate, double time){
		return -1 * strike 
				* time 
				* Math.pow(Math.E, -1 * rate * time) 
				* calcCumDist(calcD2(spot, strike, volatility, rate, time));
	}
	

	
	public static void main(String[] args){
		DecimalFormat df = new DecimalFormat("###,###,###,###,###.##################");
		double d1 = calcD1(
				12000,
				12000,
				0.2d,
				0.001,
				20/365d
		);
		System.out.println("d1:"+df.format(d1));
		
		double d2 = calcD2(
				12000,
				12000,
				0.2d,
				0.001,
				20/365d
		);
		System.out.println("d2:"+df.format(d2));
		
		double sd1 = calcProbDensity(d1);
		System.out.println("sd1:"+df.format(sd1));
		
		double sd2 = calcProbDensity(d2);
		System.out.println("sd2:"+df.format(sd2));
		

		double cd1 = calcCumDist(d1);
		System.out.println("cd1:"+df.format(cd1));
		
		double cd2 = calcCumDist(d2);
		System.out.println("cd2:"+df.format(cd2));

		double call = calcBsCall(
				12000,
				12000,
				0.2d,
				0.001,
				20/365d
		);
		System.out.println("call:"+df.format(call));
		
		double dc = deltaCall(
				12000,
				12000,
				0.2d,
				0.001,
				20/365d
		);
		System.out.println("delta:"+df.format(dc));
		
		double gm = gamma(
				12000,
				12000,
				0.2d,
				0.001,
				20/365d
		);
		System.out.println("gamma:"+df.format(gm));

		double vg = vega(
				12000,
				12000,
				0.2d,
				0.001,
				20/365d
		);
		System.out.println("vega:"+df.format(vg/100d));
		
		double tc = thetaCall(
				12000,
				12000,
				0.2d,
				0.001,
				20/365d
		);
		System.out.println("theta:"+df.format(tc/365d));
		
		double rh = rhoCall(
				12000,
				12000,
				0.2d,
				0.001,
				20/365d
		);
		System.out.println("rho:"+df.format(rh/100));
		
		
		Date dd = new Date();
		Calendar cal = new GregorianCalendar();
		cal.setTime(dd);
		cal.add(Calendar.DAY_OF_YEAR, 10);
		double tt = cal.getTimeInMillis() - dd.getTime();
		System.out.println("time:"+df.format(tt/1000/60/60/24));
	}
}
