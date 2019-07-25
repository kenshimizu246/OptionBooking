package com.tamageta.financial.math;

public interface MonteCalroListener {
	public void progress(MonteCalroProgressEvent event);
	public void calculated(MonteCalroDoneEvent event);
}
