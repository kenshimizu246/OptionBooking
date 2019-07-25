package com.tamageta.financial.math;

public class MonteCalroDoneEvent {
	private final double optionValue;
	private final double premiumValue;
	
	public MonteCalroDoneEvent(double optionValue,double premiumValue){
		this.optionValue = optionValue;
		this.premiumValue = premiumValue;
	}

	public double getOptionValue() {
		return optionValue;
	}

	public double getPremiumValue() {
		return premiumValue;
	}
}
