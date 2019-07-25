package com.tamageta.financial.booking.rfq;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.tamageta.financial.booking.dao.CacheDao;
import com.tamageta.financial.booking.dao.Dao;
import com.tamageta.financial.booking.dao.DaoException;
import com.tamageta.financial.booking.dao.DemoDao;
import com.tamageta.financial.booking.gui.ClientListDialog;
import com.tamageta.financial.booking.rfq.data.Client;
import com.tamageta.financial.util.GuiUtility;

public class ClientComboBox extends JPanel {
	private Client client = null;
	private Dao dao = null;
	private JTextField textClientCode = new JTextField(8);
	private JTextField textClientName = new JTextField(24);
	private JButton btnClient = new JButton("...");
	private String previous = null;

	public ClientComboBox(){
		FlowLayout fl = new FlowLayout(FlowLayout.LEADING, 0,0);
		setLayout(fl);
		add(textClientCode);
		add(textClientName);
		add(btnClient);
		Dimension btnSize = textClientCode.getPreferredScrollableViewportSize();
		btnSize.setSize(btnSize.getHeight(), btnSize.getHeight());
		btnClient.setPreferredSize(btnSize);
		textClientCode.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent arg0) {
				previous = textClientCode.getText();
			}

			public void focusLost(FocusEvent e) {
				if(!textClientCode.getText().equals(previous)){
					findByClientID();
				}
			}
		});
		textClientName.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent arg0) {
				previous = textClientName.getText();
			}
			public void focusLost(FocusEvent e) {
				if(!textClientName.getText().equals(previous)){
					findByName();
				}
			}
		});
		btnClient.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				findAll();
			}
		});
	}
	private void findByClientID(){
		if(textClientCode.getText() != null
				&& textClientCode.getText().length() > 0){
			try{
				Client client = dao.getClient(textClientCode.getText().toUpperCase());
				setClient(client);
				System.out.println("client:"+client);
			}catch(DaoException ex){
				ex.printStackTrace();
			}
		}
	}
	private void findByName(){
		if(textClientName.getText() != null
					&& textClientName.getText().length() > 0){
			try{
				List<Client> ll = dao.getClients(textClientName.getText().toUpperCase());
				if(ll != null && ll.size() == 1){
					setClient(ll.get(0));
				}else if(ll != null && ll.size() > 0){
					Client r = popupClientList(ll);
					if(r != null){
						setClient(r);
					}else{
						setClient((Client)null);
					}
				}
			}catch(DaoException ex){
				ex.printStackTrace();
				JOptionPane.showConfirmDialog(
						GuiUtility.getParentFrame(this), 
						ex.getMessage(), 
						"Underlying Search Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	private void findAll(){
		try{
			List<Client> ll = dao.getClients();
			Client r = popupClientList(ll);
			if(r  != null){
				setClient(r);
			}
		}catch(DaoException e){
			e.printStackTrace();
		}
	}
	ClientListDialog ud = new ClientListDialog((Frame)SwingUtilities.getRoot(this));
	private Client popupClientList(List<Client> ll){
		Collections.sort(ll, new Comparator<Client>(){
			public int compare(Client r0, Client r1) {
				if(r0 == null){
					return -1;
				}else if(r1 == null){
					return 1;
				}
				return r0.getName().compareTo(r1.getName());
			}
			
		});
		ud.setClients(ll);
		ud.pack();
		GuiUtility.centerWindow(ud, this);
		ud.setVisible(true);
		if(ud.isOK() && ud.getClient() != null){
			return ud.getClient();
		}
		return null;
	}
	
	public static void main(String[] args) {
		ClientComboBox ucb = new ClientComboBox();
		ucb.setDao(new CacheDao(new DemoDao()));
		JFrame frame = new JFrame("test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(ucb);
		frame.pack();
		frame.setVisible(true);
	}
	public Dao getDao() {
		return dao;
	}
	public void setDao(Dao dao) {
		this.dao = dao;
	}
	public Client getClient() {
		return client;
	}
	public void setClient(Client client) {
		this.client = client;
		if(client != null){
			textClientCode.setText(client.getClientId());
			textClientName.setText(client.getName());
		}else{
			textClientCode.setText("");
			textClientName.setText("");
		}
	}
	public void setClient(String clientId){
		Client client = null;
		if(clientId != null && clientId.trim().length() > 0){
			try{
				client = dao.getClient(clientId);
			}catch(DaoException ex){
				ex.printStackTrace();
			}
		}
		setClient(client);
	}
	public void setEditable(boolean b){
		textClientCode.setEditable(b);
		textClientName.setEditable(b);
		btnClient.setEnabled(b);
	}
	
	public String getClientId(){
		return (client != null ? client.getClientId() : null);
	}
}
