package com.tamageta.financial.booking.rfq.data;

public class Ric {
	private String ric;
	private String name;

	public Ric(String ric, String name) {
		this.ric  = ric;
		this.name = name;
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
}
