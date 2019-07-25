package com.tamageta.financial.booking.rfq;

import java.awt.Component;
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
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.xml.soap.SOAPException;

import com.tamageta.financial.booking.dao.CacheDao;
import com.tamageta.financial.booking.dao.Dao;
import com.tamageta.financial.booking.dao.DaoException;
import com.tamageta.financial.booking.dao.DemoDao;
import com.tamageta.financial.booking.gui.UnderlyingListDialog;
import com.tamageta.financial.booking.rfq.data.Ric;
import com.tamageta.financial.booking.rfq.data.Underlying;
import com.tamageta.financial.util.GuiUtility;

public class UnderlyingComboBox extends JPanel {
	private Underlying underlying = null;
	private Dao dao = null;
	private JTextField textUnderlyingCode = new JTextField(8);
	private JTextField textUnderlyingName = new JTextField(24);
	private JTextField textUnderlyingMIC  = new JTextField(4);
	private JButton btnUnderlying = new JButton("...");
	private Vector<UnderlyingListener> listeners = new Vector<UnderlyingListener>();
	private String previous = null;

	public UnderlyingComboBox(){
		FlowLayout fl = new FlowLayout(FlowLayout.LEADING, 0,0);
		setLayout(fl);
		add(textUnderlyingCode);
		add(textUnderlyingName);
		add(textUnderlyingMIC);
		add(btnUnderlying);
		Dimension btnSize = textUnderlyingCode.getPreferredScrollableViewportSize();
		btnSize.setSize(btnSize.getHeight(), btnSize.getHeight());
		btnUnderlying.setPreferredSize(btnSize);
		textUnderlyingMIC.setEditable(false);
		textUnderlyingCode.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent arg0) {
				previous = textUnderlyingCode.getText();
			}

			public void focusLost(FocusEvent e) {
				if(!textUnderlyingCode.getText().equals(previous)){
					findByRic();
				}
			}
		});
		textUnderlyingName.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent arg0) {
				previous = textUnderlyingName.getText();
			}
			public void focusLost(FocusEvent e) {
				if(!textUnderlyingName.getText().equals(previous)){
					findByName();
				}
			}
		});
		btnUnderlying.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				findAll();
			}
		});
	}
	private void findByRic(){
		if(textUnderlyingCode.getText() != null
				&& textUnderlyingCode.getText().length() > 0){
			try{
				Underlying ul = dao.getUnderlying(textUnderlyingCode.getText().toUpperCase());
				setUnderlying(ul);
				System.out.println("ul:"+ul);
			}catch(DaoException ex){
				ex.printStackTrace();
			}
		}
	}
	private void findByName(){
		if(textUnderlyingName.getText() != null
					&& textUnderlyingName.getText().length() > 0){
			try{
				List<Ric> ll = dao.getUnderlyingRics(textUnderlyingName.getText().toUpperCase());
				if(ll != null && ll.size() == 1){
					setUnderlying(dao.getUnderlying(ll.get(0).getRic()));
				}else if(ll != null && ll.size() > 0){
					Ric r = popupRicList(ll);
					if(r != null){
						setUnderlying(dao.getUnderlying(r.getRic()));
					}else{
						setUnderlying((Underlying)null);
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
			List<Ric> ll = dao.getUnderlyingRics(null);
			Ric r = popupRicList(ll);
			if(r  != null){
				setUnderlying(dao.getUnderlying(r.getRic()));
			}
		}catch(DaoException e){
			e.printStackTrace();
		}
	}
	UnderlyingListDialog ud = new UnderlyingListDialog((Frame)SwingUtilities.getRoot(this));
	private Ric popupRicList(List<Ric> ll){
		Collections.sort(ll, new Comparator<Ric>(){
			public int compare(Ric r0, Ric r1) {
				if(r0 == null){
					return -1;
				}else if(r1 == null){
					return 1;
				}
				return r0.getRic().compareTo(r1.getRic());
			}
			
		});
		ud.setRics(ll);
		ud.pack();
		GuiUtility.centerWindow(ud, this);
		ud.setVisible(true);
		if(ud.isOK() && ud.getRic() != null){
			return ud.getRic();
		}
		return null;
	}
	
	public static void main(String[] args) {
		UnderlyingComboBox ucb = new UnderlyingComboBox();
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
	public Underlying getUnderlying() {
		return underlying;
	}
	public void setUnderlying(Underlying underlying) {
		this.underlying = underlying;
		if(underlying != null){
			textUnderlyingCode.setText(underlying.getRic());
			textUnderlyingName.setText(underlying.getName());
			textUnderlyingMIC.setText(underlying.getMic());
		}else{
			textUnderlyingCode.setText("");
			textUnderlyingName.setText("");
			textUnderlyingMIC.setText("");
		}
		fireUnderlyingListener(new UnderlyingEvent(this, underlying));
	}
	public void setUnderlying(String ric){
		Underlying underlying = null;
		if(ric != null && ric.trim().length() > 0){
			try{
				underlying = dao.getUnderlying(ric);
			}catch(DaoException ex){
				ex.printStackTrace();
			}
		}
		setUnderlying(underlying);
	}
	public void setEditable(boolean b){
		textUnderlyingCode.setEditable(b);
		textUnderlyingName.setEditable(b);
		btnUnderlying.setEnabled(b);
	}
	
	public String getRic(){
		return (underlying != null ? underlying.getRic() : null);
	}

	public void addUnderlyingListener(UnderlyingListener listener){
		Vector<UnderlyingListener> ll = (Vector<UnderlyingListener>)listeners.clone();
		ll.add(listener);
		listeners = ll;
	}
	
	public void removeUnderlyingListener(UnderlyingListener listener){
		Vector<UnderlyingListener> ll = (Vector<UnderlyingListener>)listeners.clone();
		ll.remove(listener);
		listeners = ll;
	}
	
	public void fireUnderlyingListener(UnderlyingEvent event){
		for(UnderlyingListener l : listeners){
			l.underlynigChange(event);
		}
	}
}
