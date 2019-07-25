package com.tamageta.financial.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.SwingUtilities;

public class GuiUtility {
	public static void centerWindow(Window frame){
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension d = tk.getScreenSize();
		
		centerWindow(frame, d, new Point(0,0));
	}
	
	public static void centerWindow(Window frame, Dimension d, Point p){
		double h = (d.getHeight() - frame.getSize().getHeight())/2;
		double w = (d.getWidth()  - frame.getSize().getWidth())/2;
		
		frame.setLocation((int)(w+p.getX()),(int)(h+p.getY()));
	}
	
	public static void centerWindow(Window frame, Component c){
		Frame f = (Frame)SwingUtilities.getRoot(c);
		centerWindow(frame, f.getSize(), f.getLocation());
	}
	
	public static void centerWindowOnParentWindow(Window frame){
		Frame parent = getParentFrame(frame);
		centerWindow(frame, parent.getSize(), parent.getLocation());
	}
	
	public static Frame getParentFrame(Component c){
		while(c != null){
			if(c instanceof Frame){
				return (Frame)c;
			}
			c = c.getParent();
		}
		return null;
	}
}
