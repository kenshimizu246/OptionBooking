package com.tamageta.financial.booking.rfq.dialogs;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.ScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.tamageta.financial.booking.rfq.data.Underlying;

public class UnderlyingDialog extends AbstractDialog {
	private JPanel panelMain = new JPanel();
	private JScrollPane scrollMain = new JScrollPane();
	private JTable tableMain;
	private MyTableModel model = new MyTableModel();
	
	public UnderlyingDialog(Frame arg0, String arg1, boolean arg2) {
		super(arg0, arg1, arg2);
		tableMain = new JTable();
		tableMain.setModel(model);
		tableMain.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableMain.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent event) {
				if(event.getClickCount() > 1){
					doOK();
				}
			}
		});
		
		getContentPane().add(panelMain, BorderLayout.CENTER);
		panelMain.setLayout(new BorderLayout());
		panelMain.add(scrollMain, BorderLayout.CENTER);
		scrollMain.getViewport().add(tableMain);
	}
	public Underlying getSelectedUnderlying(){
		return null;
	}
	private class MyTableModel extends AbstractTableModel{
		private final String[] columnNames = new String[]{
				"Code","Name","MIC","Multiplier"
		};
		private Vector<Underlying> underlyings = new Vector<Underlying>();
		
		public MyTableModel(){
			;
		}
		
		public void addUnderlying(Underlying underlying){
			underlyings.add(underlying);
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return underlyings.size();
		}

		public Object getValueAt(int r, int c) {
			Underlying uu = underlyings.get(r);
			switch(c){
			case 0:
				return uu.getQuick();
			case 1:
				return uu.getName();
			case 2:
				return uu.getName();
			case 3:
				return uu.getName();
			}
			return null;
		}
	}
}
