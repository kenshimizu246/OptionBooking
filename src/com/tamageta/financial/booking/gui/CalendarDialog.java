package com.tamageta.financial.booking.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class CalendarDialog extends JDialog {
	private boolean ok = false;
	private CalendarPanel panelCalendar = new CalendarPanel();
	private JPanel panelControl = new JPanel();

	public CalendarDialog(Dialog base, String title, boolean modal) {
		super(base, title, modal);
		initGui();
	}

	public CalendarDialog(Frame base, String title, boolean modal) {
		super(base, title, modal);
		initGui();
	}
	
	private void initGui(){
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panelCalendar, BorderLayout.CENTER);
		getContentPane().add(panelControl, BorderLayout.SOUTH);
		
		JButton btnOK = new JButton("OK");
		JButton btnCancel = new JButton("Cancel");
		
		panelControl.add(btnOK);
		panelControl.add(btnCancel);
		
		btnOK.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				ok = true;
				dispose();
			}
		});
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				ok = false;
				dispose();
			}
		});
	}
	
	public void setDate(Date date){
		panelCalendar.setDate(date);
	}
	
	public Date getDate(){
		return panelCalendar.getDate();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		CalendarDialog dialog = new CalendarDialog(frame, "Calendar", true);
		dialog.pack();
		dialog.setVisible(true);
	}
}
