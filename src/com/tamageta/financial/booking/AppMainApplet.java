package com.tamageta.financial.booking;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.Permission;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.tamageta.financial.booking.dao.Dao;
import com.tamageta.financial.booking.dao.DemoDao;
import com.tamageta.financial.booking.rfq.RFQPricingFrame;
import com.tamageta.financial.util.GuiUtility;

public class AppMainApplet extends Applet {
	private JFrame startupFrame = new JFrame();
	private Dao dao = new DemoDao();
	private final JButton btnStart;

	public AppMainApplet(){
		setLayout(new BorderLayout());
		btnStart = new JButton("Start 1...");
		btnStart.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("start 1...");
				Thread t = new Thread(){
					public void run(){
						startDemo();
					}
				};
				t.start();
			}
		});
		add(btnStart, BorderLayout.CENTER);
	}

	@Override
	public void init() {
		super.init();
	}
	
	@Override
	public void start() {
		super.start();
		SecurityManager sm = new MySecurityManager();
		System.setSecurityManager(sm);
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}

	private void startDemo(){
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
			
			String userID = "demoTrader";
			
			dao.login(userID
					, new char[]{});
			Environment.setUserID(userID);
			
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
	private class MySecurityManager extends SecurityManager {
	    @Override
	    public void checkPermission(Permission perm) {
	        return;
	    }
	}
}
