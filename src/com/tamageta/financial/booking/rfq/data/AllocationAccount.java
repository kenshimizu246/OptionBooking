package com.tamageta.financial.booking.rfq.data;

public class AllocationAccount {
	private final String account;
	private String name;
	private boolean valid = true;
	
	public AllocationAccount(String account, String name){
		this.account = account;
		this.name    = name;
	}

	public String getAccount() {
		return account;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public boolean isValid(){
		return valid;
	}
	
	public void setValid(boolean valid){
		this.valid = valid;
	}
	
	public String toString(){
		return name + " [" + account + "] " + (valid?"":"INVALID");
	}
}
