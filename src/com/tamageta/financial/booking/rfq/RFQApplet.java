package com.tamageta.financial.booking.rfq;

import java.applet.Applet;
import java.awt.BorderLayout;

import javax.swing.JLabel;

import com.tamageta.financial.booking.dao.CacheDao;
import com.tamageta.financial.booking.dao.SoapDao;

public class RFQApplet extends Applet {
	private RFQMenuPanel panelMenu = new RFQMenuPanel();
	private JLabel labelStatus = new JLabel("xyz");
	
	public RFQApplet(){
		setLayout(new BorderLayout());
		panelMenu = new RFQMenuPanel();
		add(panelMenu,BorderLayout.CENTER);
		add(labelStatus,BorderLayout.SOUTH);
	}
	public void init() {
		super.init();
		String url = getParameter("url");
		labelStatus.setText("DAO:"+url);
		try{
			panelMenu.setDao(new CacheDao(new SoapDao(url)));
		}catch(Exception e){
			labelStatus.setText(e.getMessage());
		}
	}
}
