package com.tamageta.financial.booking.rfq.data;

public class Counterparty {
	private final long id;
	private String status;
	private final String companyId;
	private final String account;
	private String name;
	
	public static final String STATUS_ACTIVE   = "active";
	public static final String STATUS_DISABLED = "disabled";

	public Counterparty(long id, String companyId, String status, String account, String name){
		this.id = id;
		this.account = account;
		this.companyId = companyId;
		this.name = name;
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public String getAccount() {
		return account;
	}

	public String getCompanyId() {
		return companyId;
	}
	
	public String getStatus(){
		return status;
	}
	
	public void setStatus(String status){
		this.status = status;
	}
}