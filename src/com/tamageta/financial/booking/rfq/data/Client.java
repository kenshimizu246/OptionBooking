package com.tamageta.financial.booking.rfq.data;

public class Client {
	public final long id;
	public final String clientId;
	public final String name;
	public String status;
	
	public Client(long id, String clientId, String name, String status){
		this.id = id;
		this.clientId = clientId;
		this.name = name;
		this.status = status;
	}

	public String getClientId() {
		return clientId;
	}

	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getId() {
		return id;
	}
}
