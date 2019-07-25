package com.tamageta.financial.trading.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;


import quickfix.ConfigError;

public class QuickFixDaoTestFrame extends JFrame {
	private final QuickFixDao dao;
	
	public QuickFixDaoTestFrame(QuickFixDao dao){
		this.dao = dao;
		this.getContentPane().setLayout(new BorderLayout());
		final JButton btn1 = new JButton("OK");
		btn1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				logOff();
			}
		});
		final JTextField field1 = new JTextField(10);
		final JTextField field2 = new JTextField(10);
		
		JPanel panel = new JPanel();
		panel.add(field1);
		panel.add(field2);
		
		FocusTraversalPolicy policy = new FocusTraversalPolicy() {
			  private final List<? extends Component> order = Arrays.asList(btn1,field2,field1);
			  @Override
			  public Component getFirstComponent(Container focusCycleRoot) {
			    return order.get(0);
			  }
			  @Override
			  public Component getLastComponent(Container focusCycleRoot) {
			    return order.get(order.size()-1);
			  }
			  @Override
			  public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
			    int i = order.indexOf(aComponent);
			    return order.get((i + 1) % order.size());
			  }
			  @Override
			  public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
			    int i = order.indexOf(aComponent);
			    return order.get((i - 1 + order.size()) % order.size());
			  }
			  @Override
			  public Component getDefaultComponent(Container focusCycleRoot) {
			    return order.get(0);
			  }
			};

		setFocusTraversalPolicy(policy);
		getContentPane().add(panel, BorderLayout.CENTER);
		getContentPane().add(btn1, BorderLayout.SOUTH);
	}
	
	public void logOff(){
		dao.logOff();
	}

	public static void main(String[] args) {
		QuickFixDao dao = new QuickFixDao();
		dao.setConfigFileName("etc/fixClient.conf");
		try {
			dao.init();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ConfigError e) {
			e.printStackTrace();
		}
		
		QuickFixDaoTestFrame frame = new QuickFixDaoTestFrame(dao);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
