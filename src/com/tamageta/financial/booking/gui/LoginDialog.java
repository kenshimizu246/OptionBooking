package com.tamageta.financial.booking.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JLabel;

public class LoginDialog extends JDialog {
	private boolean ok = false;
	private JPanel panelMain = new JPanel();
	private JPanel panelControl = new JPanel();
	private JButton btnOK = new JButton("OK");
	private JButton btnCancel = new JButton("Cancel");
	private JTextField textID = new JTextField(24);
	private JPasswordField textPWD = new JPasswordField(24);
	
	public LoginDialog(java.awt.Window window){
		super(window, ModalityType.MODELESS);
		initGui();
	}
	public LoginDialog(Frame frame){
		super(frame,true);
		initGui();
	}
	
	private void initGui(){
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panelMain, BorderLayout.CENTER);
		getContentPane().add(panelControl, BorderLayout.SOUTH);
		
		panelControl.add(btnOK);
		panelControl.add(btnCancel);
		
		btnOK.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				ok = true;
				dispose();
			}
		});
		btnOK.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				ok = false;
				dispose();
			}
		});
		textID.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent event) {
				checkEnabled();
				if(event.getKeyChar() == '\n'
					&& btnOK.isEnabled()){
					ok = true;
					dispose();
				}
			}
		});
		textPWD.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent event) {
				checkEnabled();
				if(event.getKeyChar() == '\n'
					&& btnOK.isEnabled()){
					ok = true;
					dispose();
				}
			}
		});
		btnOK.setEnabled(false);
		
		GridBagLayout layoutMain = new GridBagLayout();
		panelMain.setLayout(layoutMain);
		
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        JLabel labelUserID = new JLabel("User ID:");
        layoutMain.setConstraints(labelUserID, gbc);
		panelMain.add(labelUserID);
		
        gbc.gridx = 1;
        gbc.gridy = 0;
        layoutMain.setConstraints(textID, gbc);
		panelMain.add(textID);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel labelPwd = new JLabel("Password:");
        layoutMain.setConstraints(labelPwd, gbc);
		panelMain.add(labelPwd);
		
        gbc.gridx = 1;
        gbc.gridy = 1;
        layoutMain.setConstraints(textPWD, gbc);
		panelMain.add(textPWD);
	}
	
	private void checkEnabled(){
		if(textID.getText().length() > 0 
				&& textPWD.getPassword().length > 0){
			btnOK.setEnabled(true);
		}else{
			btnOK.setEnabled(false);
		}
	}
	
	public String getID(){
		return textID.getText();
	}
	
	public char[] getPassword(){
		return textPWD.getPassword();
	}
	
	public boolean isOK(){
		return ok;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		javax.swing.JFrame frame = new javax.swing.JFrame();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		LoginDialog dialog = new LoginDialog(frame);
		dialog.setVisible(true);

	}

}
