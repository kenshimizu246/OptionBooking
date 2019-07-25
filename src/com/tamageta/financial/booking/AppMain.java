package com.tamageta.financial.booking;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import com.tamageta.financial.booking.dao.CacheDao;
import com.tamageta.financial.booking.dao.Dao;
import com.tamageta.financial.booking.dao.DemoDao;
import com.tamageta.financial.booking.dao.LRUCacheDao;
import com.tamageta.financial.booking.dao.SoapDao;
import com.tamageta.financial.booking.gui.LoginDialog;
import com.tamageta.financial.booking.rfq.RFQPricingFrame;
import com.tamageta.financial.util.GuiUtility;

public class AppMain {
	private JFrame startupFrame = new JFrame();
	
	public AppMain(String[] args){
		Dao dao = null;
		try{
			startupFrame.setUndecorated(true);
			ImageIcon icon = new ImageIcon(AppMain.class.getResource("startup.jpg"));
			startupFrame.getContentPane().setLayout(new BorderLayout());
			startupFrame.getContentPane().add(new JLabel(icon), BorderLayout.CENTER);
			JProgressBar pb = new JProgressBar();
			pb.setIndeterminate(true);
			startupFrame.getContentPane().add(pb, BorderLayout.SOUTH);

			startupFrame.pack();
			GuiUtility.centerWindow(startupFrame);
			startupFrame.setVisible(true);
			

			String userID = null;
			if(args.length == 0){
				dao = new DemoDao();
				userID = "demoTrader";
				dao.login(userID, new char[]{});
				Environment.setUserID(userID);
			}else{
				dao = new LRUCacheDao(new SoapDao(args[0]));
				LoginDialog loginDialog = new LoginDialog(startupFrame);
				loginDialog.pack();
				GuiUtility.centerWindow(loginDialog, startupFrame.getSize(), startupFrame.getLocation());
				loginDialog.setVisible(true);
				if(loginDialog.isOK()){
					try{
						dao.login(loginDialog.getID()
								, loginDialog.getPassword());
						userID = loginDialog.getID();
						Environment.setUserID(userID);
						if(dao instanceof LRUCacheDao){
							((LRUCacheDao)dao).preCache();
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					JOptionPane.showMessageDialog(startupFrame,
							"Please login again!",
							"Login Failed",
							JOptionPane.ERROR_MESSAGE);
					startupFrame.setVisible(false);
				}
			}
			
			RFQPricingFrame frame = new RFQPricingFrame();
			frame.setDao(dao);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
			frame.setSize(840,680);
			GuiUtility.centerWindow(frame);
			frame.setVisible(true);
			startupFrame.setVisible(false);
		}catch(Exception e){
			e.printStackTrace();
			
		}
	}
	public static void main(String[] args) {
		AppMain app = new AppMain(args);
	}

}
