package com.tamageta.financial.math;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tamageta.financial.booking.gui.TaskCancelEvent;
import com.tamageta.financial.booking.gui.TaskCancelListener;
import java.util.logging.*;

public class MonteCalro implements Runnable, TaskCancelListener{
	private Logger logger = Logger.getLogger("MonteCalro");
	private final int days;
	private final double volatility;
	private final double rate;
	private final double spot;
	private final double strike;
	private final int count;
	private final boolean isCall;

	private final double t1;
	private final double t2;
	private final double drift;
	
	private double optionValue  = Double.NaN;
	private double premiumValue = Double.NaN;
	private volatile boolean cancel = false;
	private final Random random;
	
	private transient Vector<MonteCalroListener> listeners = new Vector<MonteCalroListener>();
	
	private class PostProgressEvent implements Runnable{
		private final MonteCalroProgressEvent event;
		private final Vector<MonteCalroListener> listeners;
		public PostProgressEvent(MonteCalroProgressEvent event, Vector<MonteCalroListener> listeners){
			this.event = event;
			this.listeners = listeners;
		}
		public void run(){
			for(MonteCalroListener listener : listeners){
				listener.progress(event);
			}
		}
	}
	public MonteCalro(int days, double spot, double strike, double rate, double volatility, boolean isCall, int count){
		this(days, spot, strike, rate, volatility, isCall, count, null);
	}
	public MonteCalro(int days, double spot, double strike, double rate, double volatility, boolean isCall, int count, Random random){
		this.days = days;
		this.spot = spot;
		this.strike = strike;
		this.rate = rate;
		this.volatility = volatility;
		this.isCall = isCall;
		this.count = count;
		
		this.t1 = days/365d;
		this.t2 = Math.sqrt(t1);
		this.drift = rate - (Math.pow(volatility, 2d)/2d);
		
		if(random != null){
			this.random = random;
		}else{
			this.random = new Random(){
				public double nextDouble() {
					return Math.random();
				}
				public double nextGaussian() {
					return Probability.inverseNormal(Math.random());
				}
			};
		}
	}
	
	public void run(){
		ExecutorService executor = Executors.newSingleThreadExecutor();

		double avg = 0.0d;
		double sum = 0.0d;
		double min = 9999999999999999d;
		double max = 0.0d;
		
		for(int i = 0; i < count && !cancel; i++){
			double rn = random.nextGaussian();
			double dd = Math.exp((rn * t2 * volatility) + (drift * t1) + Math.log(spot)); 
			double value = Math.max(isCall ? (dd - strike) : (strike - dd), 0);
			sum += value;
			avg += (value / count);
			
			min = Math.min(min, dd);
			max = Math.max(max, dd);

			executor.execute(new PostProgressEvent(
					new MonteCalroProgressEvent(i+1, count), listeners));
			
			logger.log(Level.FINE, 
			//System.out.println(
					"rn:"+rn+"\n"+
					"rn*t2:"+(rn*t2)+"\n"+
					"rn*t2*v:"+(rn*t2*volatility)+"\n"+
					"drift*t1:"+(drift * t1)+"\n"+
					"v1:"+((rn*t2*volatility)+(drift * t1))+"\n"+
					"spot:"+Math.log(spot)+"\n"+
					"vv:"+(Math.log(spot)+((rn*t2*volatility)+(drift * t1)))+"\n"+
					"dd:"+dd+"\n"+
					"value:"+value+"\n"+
					"spot:"+spot+"\n"+
					"average:"+avg
					);
		}
		optionValue = avg; //sum/count;
		premiumValue = optionValue * Math.exp(rate * t1);

		for(MonteCalroListener listener : listeners){
			listener.calculated(new MonteCalroDoneEvent(optionValue, premiumValue));
		}
	}
	public void cancel(TaskCancelEvent event) {
		cancel = true;
	}
	public void addMonteCalroListener(MonteCalroListener listener){
		Vector<MonteCalroListener> l = (Vector<MonteCalroListener>)listeners.clone();
		l.addElement(listener);
		listeners = l;
	}
	public void removeMonteCalroListener(MonteCalroListener listener){
		listeners.removeElement(listener);
	}

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

	public int getCount() {
		return count;
	}

	public int getDays() {
		return days;
	}

	public boolean isCall() {
		return isCall;
	}

	public double getRate() {
		return rate;
	}

	public double getSpot() {
		return spot;
	}

	public double getStrike() {
		return strike;
	}

	public double getVolatility() {
		return volatility;
	}

	public double getOptionValue() {
		return optionValue;
	}

	public double getPremiumValue() {
		return premiumValue;
	}
	public double getDrift() {
		return drift;
	}
}
