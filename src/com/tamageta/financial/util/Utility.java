package com.tamageta.financial.util;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.JFrame;

public class Utility {
	public static Window getWindow(Component comp){
		if(comp instanceof Window){
			return (Window)comp;
		}else if(comp == null 
				|| comp.getParent() == null 
				|| comp == comp.getParent()){
			return null;
		}
		return getWindow(comp.getParent());
	}
}
