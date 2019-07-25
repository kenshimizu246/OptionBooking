package com.tamageta.financial.booking.rfq.dialogs;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class AbstractDialog extends JDialog {
	private boolean ok = false;
	private JPanel panel = new JPanel();
	private JButton btnOK = new JButton("OK");
	private JButton btnCancel = new JButton("Cancel");

	public AbstractDialog(Frame arg0, String arg1, boolean arg2) {
		super(arg0, arg1, arg2);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.add(btnOK);
		panel.add(btnCancel);
		btnOK.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				doOK();
			}
		});
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				doCancel();
			}
		});
	}
	
	public void doOK(){
		ok = true;
		dispose();
	}
	
	public void doCancel(){
		ok = false;
		dispose();
	}
	
	public void setOkEnabled(boolean b){
		btnOK.setEnabled(b);
	}
	
	public boolean isOK(){
		return ok;
	}
}
