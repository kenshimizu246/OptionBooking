package com.tamageta.financial.booking.rfq;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class DividendTableModel extends AbstractTableModel {
	public static final List<Dividend> list = new Vector<Dividend>();
	public static final String[] columnNames = new String[]{
		"Date",
		"Dividend",
		"Spot Value"
	};
	public int getColumnCount() {
		return columnNames.length;
	}
	
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public boolean isCellEditable(int row, int col) {
		if(col < 2){
			return true;
		}
		return false;
	}

	public void setValueAt(Object value, int row, int col) {
		if(list.size() <= row){
			return;
		}
		Dividend dd = list.get(row);
		switch(col){
		case 0:
			dd.setDate((Date)value);
			break;
		case 1:
			dd.setDividend(((Number)value).doubleValue());
			break;
		}
	}

	public int getRowCount() {
		return list.size();
	}

	public Object getValueAt(int row, int col) {
		if(list.size() <= row){
			return null;
		}
		Dividend dd = list.get(row);
		switch(col){
		case 0:
			return dd.getDate();
		case 1:
			return dd.getDividend();
		case 2:
			return dd.getSpotValue();
		}
		return null;
	}

}
