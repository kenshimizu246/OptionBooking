package com.tamageta.financial.booking.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.tamageta.financial.booking.rfq.data.Client;

public class ClientListDialog extends JDialog {
	private boolean ok = false;
	private JPanel panelMain = new JPanel();
	private JPanel panelCtrl = new JPanel();
	private JPanel panelSrch = new JPanel();
	
	private JButton btnOK = new JButton("OK");
	private JButton btnCancel = new JButton("Cancel");
	
	private JScrollPane scrollList = new JScrollPane();
	private ClientTableModel model = new ClientTableModel();
	private JTable tableList = new JTable(model);
	
	
	private class ClientTableModel extends AbstractTableModel {
		private List<Client> clients = new Vector<Client>();
		private final String[] columnNames = new String[]{
			"ID",
			"Name"
		};
		
		public ClientTableModel(){

		}
		
		public void setClients(List<Client> clients){
			this.clients = clients;
			fireTableDataChanged();
		}
		
		public String getColumnName(int arg0) {
			return columnNames[arg0];
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return (clients != null ? clients.size() : 0);
		}

		public Object getValueAt(int r, int c) {
			try{
				Client client = clients.get(r);
				switch(c){
				case 0:
					return client.getClientId();
				case 1:
					return client.getName();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		public Client getClient(int row){
			return clients.get(row);
		}
	}

	public ClientListDialog(Frame frame){
		super(frame, "Client List", true);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panelSrch, BorderLayout.NORTH);
		getContentPane().add(panelMain, BorderLayout.CENTER);
		getContentPane().add(panelCtrl, BorderLayout.SOUTH);
		
		tableList.getColumnModel().getColumn(0).setPreferredWidth(48);
		tableList.getColumnModel().getColumn(1).setPreferredWidth(248);
		
		panelCtrl.add(btnOK);
		panelCtrl.add(btnCancel);
		btnOK.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				ok = true;
				dispose();
			}
		});
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				ok = false;
				dispose();
			}
		});
		
		panelMain.setLayout(new BorderLayout());
		panelMain.add(scrollList, BorderLayout.CENTER);
		scrollList.getViewport().add(tableList);
		setPreferredSize(new Dimension(320,240));
		tableList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		try{
			ClientListDialog dialog = new ClientListDialog(frame);
			dialog.pack();
			dialog.setVisible(true);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public boolean isOK(){
		return ok;
	}
	public void setClients(List<Client> clients){
		model.setClients(clients);
	}
	public Client getClient(){
		return model.getClient(tableList.getSelectedRow());
	}
}
