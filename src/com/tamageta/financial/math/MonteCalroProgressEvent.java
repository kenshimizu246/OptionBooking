package com.tamageta.financial.math;

public class MonteCalroProgressEvent {
	private final int value;
	private final int max;
	public MonteCalroProgressEvent(int value, int max){
		this.value = value;
		this.max = max;
	}
	public int getMax() {
		return max;
	}
	public int getValue() {
		return value;
	}
}
