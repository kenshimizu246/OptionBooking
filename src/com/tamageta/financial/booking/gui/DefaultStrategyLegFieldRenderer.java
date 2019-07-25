package com.tamageta.financial.booking.gui;

import java.awt.Color;
import java.awt.Component;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public class DefaultStrategyLegFieldRenderer extends DefaultTableCellRenderer {
	NumberFormat nf = new DecimalFormat("####,####,####.#####");
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static Color tableBg, tableBgSel, tableBgEd, tableBgEdSel;
	static{
		UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
		tableBg = uid.getColor("Table.background");
		tableBgSel = uid.getColor("Table.selectionBackground");
		tableBgEd = new Color(106,163,255);
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if(value instanceof Number){
			if(value instanceof Double && Double.isNaN((Double)value)){
				value = "";
			}else {
				value = nf.format(value);
			}
		}else if(value instanceof Date){
			value = df.format((Date)value);
		}
		Component cc = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if(table.isCellEditable(row, column)){
			if(isSelected){
				setBackground(tableBgEd);
			}else{
				setBackground(tableBgEd.brighter());				
			}
		}else{
			if(isSelected){
				setBackground(tableBgSel);
			}else{
				setBackground(tableBg);								
			}
		}
		return cc;
	}

	protected void setValue(Object value) {
		if(value instanceof Number){
			if(value instanceof Double && Double.isNaN((Double)value)){
				value = "";
			}else {
				value = nf.format((Double)value);
			}
		}else if(value instanceof Date){
			value = df.format((Date)value);
		}
		super.setValue(value);
	}
}
