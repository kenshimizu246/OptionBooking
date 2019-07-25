package com.tamageta.financial.booking;

public class Environment {
	private static String userID = null;
	
	public static void setUserID(String userID){
		Environment.userID = userID;
	}
	
	public static String getUserID(){
		return userID;
	}
}
