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

import com.tamageta.financial.booking.dao.CacheDao;
import com.tamageta.financial.booking.dao.Dao;
import com.tamageta.financial.booking.dao.DemoDao;
import com.tamageta.financial.booking.dao.SoapDao;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.Ric;
import com.tamageta.financial.booking.rfq.data.Underlying;

public class UnderlyingListDialog extends JDialog {
	private boolean ok = false;
	private JPanel panelMain = new JPanel();
	private JPanel panelCtrl = new JPanel();
	private JPanel panelSrch = new JPanel();
	
	private JButton btnOK = new JButton("OK");
	private JButton btnCancel = new JButton("Cancel");
	
	private JScrollPane scrollList = new JScrollPane();
	private UndTableModel model = new UndTableModel();
	private JTable tableList = new JTable(model);
	
	
	private class UndTableModel extends AbstractTableModel {
		private List<Ric> rics = new Vector<Ric>();
		private final String[] columnNames = new String[]{
			"RIC",
			"Name"
		};
		
		public UndTableModel(){

		}
		
		public void setRics(List<Ric> rics){
			this.rics = rics;
			fireTableDataChanged();
		}
		
		@Override
		public String getColumnName(int arg0) {
			return columnNames[arg0];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return (rics != null ? rics.size() : 0);
		}

		@Override
		public Object getValueAt(int r, int c) {
			try{
				Ric u = rics.get(r);
				switch(c){
				case 0:
					return u.getRic();
				case 1:
					return u.getName();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		public Ric getRicw(int row){
			return rics.get(row);
		}
	}

	public UnderlyingListDialog(Frame frame){
		super(frame, "Underlying List", true);
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
			UnderlyingListDialog dialog = new UnderlyingListDialog(frame);
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
	public void setRics(List<Ric> rics){
		model.setRics(rics);
	}
	public Ric getRic(){
		return model.getRicw(tableList.getSelectedRow());
	}
}
