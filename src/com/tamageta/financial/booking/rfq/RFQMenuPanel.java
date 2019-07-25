package com.tamageta.financial.booking.rfq;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.tamageta.financial.booking.dao.Dao;
import com.tamageta.financial.booking.gui.LoginDialog;
import com.tamageta.financial.util.Utility;

public class RFQMenuPanel extends JPanel {
	private final RFQPricingFrame frame;
	private final JButton btnRfq;
	private Thread currentCommand = null;
	
	public RFQMenuPanel(){
		btnRfq = new JButton("RFQ");
		frame = new RFQPricingFrame();
		frame.pack();
		btnRfq.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//executeCommand(new OpenRFQ());
			}
		});
		add(btnRfq);
	}
	
	public void setDao(Dao dao)throws Exception {
		frame.setDao(dao);
	}
	
	public void executeCommand(Thread t){
		if(currentCommand != null && currentCommand.isAlive()){
			currentCommand.interrupt();
		}
		currentCommand = t;
		currentCommand.start();
	}
	
//	private class OpenRFQ extends Thread {
//		public void run(){
//			String userID = null;
//			try{
//				btnRfq.setEnabled(false);
//				frame.setVisible(true);
//				frame.doLogin();
//			}finally{
//				btnRfq.setEnabled(true);
//			}
//		}
//	}
	public static void main(String[] args){
		JFrame frame = new JFrame();
		frame.add(new RFQMenuPanel());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
