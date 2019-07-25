package com.tamageta.financial.booking.rfq.data;

public class Allocation {
	private final StrategyLeg leg;
	private final long id;
	private AllocationAccount allocAccount;
	private double amount;
	private double percentage;
	
	public Allocation(long id, StrategyLeg leg, AllocationAccount allocAccount, double amount){
		if(leg == null){
			throw new IllegalArgumentException("StrategyLeg must not be null!");
		}
		this.id = id;
		this.leg = leg;
		this.amount = amount;
		this.allocAccount = allocAccount;
		calcPercentage();
	}
	
	public void calcPercentage(){
		percentage = amount / (leg.getPremium() * leg.getQuantity());
	}
	
	public void calcAmount(){
		amount = (leg.getPremium() * leg.getQuantity()) * percentage;
	}
	
	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getPercentage() {
		return percentage;
	}

	public void setPercentage(double percentage) {
		if(percentage > 1){
			throw new IllegalArgumentException("Percentage must be less than 1. ["+percentage+"]");
		}
		this.percentage = percentage;
	}

	public long getId() {
		return id;
	}

	public StrategyLeg getStrategyLeg() {
		return leg;
	}

	public AllocationAccount getAccount() {
		return allocAccount;
	}
	
	public void setAccount(AllocationAccount allocAccount){
		this.allocAccount = allocAccount;
	}
}
