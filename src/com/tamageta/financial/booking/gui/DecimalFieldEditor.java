package com.tamageta.financial.booking.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

public class DecimalFieldEditor extends DefaultCellEditor implements
		TableCellEditor {
	private final DecimalField ftf;
	
	public DecimalFieldEditor(){
		super(new DecimalField());
        ftf = (DecimalField)getComponent();
        ftf.getInputMap().put(KeyStroke.getKeyStroke(
                                        KeyEvent.VK_ENTER, 0),
                                        "check");
        ftf.getActionMap().put("check", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
		if (!ftf.isEditValid()) { //The text is invalid.
                    if (userSaysRevert()) { //reverted
		        ftf.postActionEvent(); //inform the editor
		    }
                } else try {              //The text is valid,
                    ftf.commitEdit();     //so use it.
                    ftf.postActionEvent(); //stop editing
                } catch (java.text.ParseException exc) { }
            }
        });
        setClickCountToStart(1);
	}
	
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
        JFormattedTextField ftf =
            (JFormattedTextField)super.getTableCellEditorComponent(
                table, value, isSelected, row, column);
        if(value instanceof Double && Double.isNaN((Double)value)){
        	ftf.setValue(null);
        }else{
        	ftf.setValue(value);
        	if(ftf.getText() != null){
	        	ftf.setSelectionStart(0);
	        	ftf.setSelectionEnd(ftf.getText().length());
        	}
        }
        return ftf;
	}

	public Object getCellEditorValue() {
        JFormattedTextField ftf = (JFormattedTextField)getComponent();
        Object o = ftf.getValue();
        if (o instanceof Double) {
            return o;
        } else if (o instanceof Number) {
            return new Double(((Number)o).doubleValue());
        } else {
//            if (DEBUG) {
//                System.out.println("getCellEditorValue: o isn't a Number");
//            }
//            try {
//                return integerFormat.parseObject(o.toString());
//            } catch (ParseException exc) {
//                System.err.println("getCellEditorValue: can't parse o: " + o);
//                return null;
//            }
        }
        return null;
	}
    public boolean stopCellEditing() {
        JFormattedTextField ftf = (JFormattedTextField)getComponent();
        if (ftf.isEditValid()) {
            try {
                ftf.commitEdit();
            } catch (java.text.ParseException exc) { }
	    
        } else { //text is invalid
            if (!userSaysRevert()) { //user wants to edit
		        return false; //don't let the editor go away
		    } 
        }
        return super.stopCellEditing();
    }

    protected boolean userSaysRevert() {
        Toolkit.getDefaultToolkit().beep();
        ftf.selectAll();
        Object[] options = {"Edit",
                            "Revert"};
        int answer = JOptionPane.showOptionDialog(
            SwingUtilities.getWindowAncestor(ftf),
            "You can either continue editing "
            + "or revert to the last valid value.",
            "Invalid Text Entered",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[1]);
	    
        if (answer == 1) { //Revert!
            ftf.setValue(ftf.getValue());
	    return true;
        }
	return false;
    }
}
