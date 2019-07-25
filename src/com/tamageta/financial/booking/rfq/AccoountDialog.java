package com.tamageta.financial.booking.rfq;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.tamageta.financial.booking.dao.Dao;
import com.tamageta.financial.booking.dao.DaoException;
import com.tamageta.financial.booking.rfq.data.AllocationAccount;

public class AccoountDialog extends JDialog {
	private final AccountModel modelAlloc;
	private final JTable tableAlloc;
	private Dao dao = null;
	private boolean ok = false;

	private class AccountModel  extends AbstractTableModel {
		private static final int IDX_ACCT  = 0;
		private static final int IDX_NAME  = 1;
		private static final int IDX_VALD  = 2; 
		
		private final String[] titles = new String[]{
			"Account","Name","Valid"
		};
		private Vector<AllocationAccount> recs = new Vector<AllocationAccount>();
		
		public void setAccounts(List<AllocationAccount> accts){
			recs.clear();
			for(AllocationAccount acct : accts){
				recs.add(acct);
			}
		}
		public List<AllocationAccount> getAccounts(){
			return (List<AllocationAccount>)recs.clone();
		}
		public void addAccount(AllocationAccount account){
			recs.add(account);
		}
		public void removeAccount(AllocationAccount account){
			recs.remove(account);
		}

		public String getColumnName(int idx) {
			return titles[idx];
		}
		public int getColumnCount() {
			return titles.length;
		}
		public int getRowCount() {
			return recs.size();
		}
		public boolean isCellEditable(int row, int col) {
			switch(col){
			case IDX_ACCT:
				return false;
			case IDX_NAME:
			case IDX_VALD:
				return true;
			}
			return false;
		}
		public void setValueAt(Object value, int row, int col) {
			AllocationAccount acct = recs.get(row);
			switch(col){
			case IDX_NAME:
				acct.setName((String)value);
				break;
			case IDX_VALD:
				acct.setValid((Boolean)value);
				break;
			}
		}
		public Object getValueAt(int row, int col) {
			AllocationAccount acct = recs.get(row);
			switch(col){
			case IDX_ACCT:
				return acct.getAccount();
			case IDX_NAME:
				return acct.getName();
			case IDX_VALD:
				return acct.isValid();
			}
			return null;
		}
		public Class getColumnClass(int col) {
			switch(col){
			case IDX_ACCT:
				return String.class;
			case IDX_NAME:
				return String.class;
			case IDX_VALD:
				return Boolean.class;
			}
			return String.class;
		}
	}
	
	public AccoountDialog(Frame parent){
		super(parent, "Account Maintenance", true);
		setLayout(new BorderLayout());

		modelAlloc = new AccountModel();
		tableAlloc = new JTable(modelAlloc);
		tableAlloc.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumn col = tableAlloc.getColumnModel().getColumn(AccountModel.IDX_ACCT);
		col.setPreferredWidth(80);
		col = tableAlloc.getColumnModel().getColumn(AccountModel.IDX_NAME);
		col.setPreferredWidth(360);
		JScrollPane scrollAlloc = new JScrollPane(tableAlloc);
		add(scrollAlloc, BorderLayout.CENTER);
		
		final JPopupMenu popup = new JPopupMenu();
		JMenuItem pmiAdd = new JMenuItem("Add");
		
		popup.add(pmiAdd);
		
		pmiAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				modelAlloc.addAccount(new AllocationAccount("", ""));
			}
		});
		
		MouseAdapter ma = new MouseAdapter() {
			public void mousePressed(MouseEvent event){
				if(event.isPopupTrigger()){
					popup.show(event.getComponent(),
							event.getX(), event.getY());
				}
			}
		};
		//tableAlloc.addMouseListener(ma);
		//scrollAlloc.addMouseListener(ma);

		JPanel panelControl = new JPanel();
		JButton btnOK     = new JButton("OK");
		JButton btnCancel = new JButton("Cancel");
		panelControl.add(btnOK);
		panelControl.add(btnCancel);
		getContentPane().add(panelControl, BorderLayout.SOUTH);
		
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
		if(dao != null){
			try{
				dao.saveAccount(getAcounts());
			}catch(DaoException e){
				JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		}
		ok = true;
		dispose();
	}
	
	public void doCancel(){
		ok = false;
		dispose();
	}
	
	public List<AllocationAccount> getAcounts(){
		return modelAlloc.getAccounts();
	}
	
	public void setAccounts(List<AllocationAccount> accounts){
		modelAlloc.setAccounts(accounts);
	}
	
	public void setDao(Dao dao) throws DaoException {
		this.dao = dao;
		setAccounts(dao.getAccounts());
	}
}
