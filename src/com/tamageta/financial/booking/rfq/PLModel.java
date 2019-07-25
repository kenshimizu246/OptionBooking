package com.tamageta.financial.booking.rfq;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.tamageta.financial.booking.dao.Dao;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.StrategyLeg;

public class PLModel extends AbstractTableModel {
	private final Dao dao;
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final String[] colms = new String[]{
		"Underlying",
		"C/P",
		"Strike",
		"Expiry",
		"Position",
		"Amount"
	};
	
	private class PnlKey implements Comparable<PnlKey>{

		final String underlyingCode;
		final String callOrPut;
		final double strike;
		final String expiry;
		
		public PnlKey(String underlyingCode, String callOrPut, double strike, String expiry){
			this.underlyingCode = underlyingCode;
			this.callOrPut = callOrPut;
			this.strike = strike;
			this.expiry = expiry;
		}
		
		public boolean equals(Object o){
			if(o instanceof PnlKey){
				PnlKey pk = (PnlKey)o;
				return pk.underlyingCode.equals(underlyingCode)
						&& pk.callOrPut.equals(callOrPut)
						&& pk.strike == strike
						&& pk.expiry.equals(expiry)
				;
			}
			return false;
		}

		public int hashCode() {
			return (underlyingCode.hashCode()
					+ callOrPut.hashCode()
					+ (int)strike
					+ expiry.hashCode());
		}

		public int compareTo(PnlKey o) {
			return underlyingCode.compareTo(o.underlyingCode)
				   + callOrPut.compareTo(o.callOrPut)
				   + (int)(strike - o.strike)
				   + expiry.compareTo(o.expiry)
			;
		}
	}
	private class Balance {
		double position = 0;
		double balance = 0;
	}
	private HashMap<PnlKey, Balance> pnl = new HashMap<PnlKey,Balance>();
	private List<PnlKey> keys = new ArrayList<PnlKey>();
	
	public PLModel(Dao dao){
		this.dao = dao;
		
		try{
			for(Enumeration<Quote> e = dao.getQuotes(); e.hasMoreElements();){
				Quote q = e.nextElement();
				
				if(!Quote.STATUS_DONE.equals(q.getStatus())){
					continue;
				}
				
				boolean ls = (Quote.SELL.equals(q.getBuyOrSell()));
				for(StrategyLeg leg : q.getStrategies()){
					PnlKey key = new PnlKey(q.getUnderlyingCode()
									,leg.getCallOrPut()
									,leg.getStrike()
									,df.format(leg.getExpiry()));
					if(!pnl.containsKey(key)){
						pnl.put(key, new Balance());
					}
					Balance b = pnl.get(key);
					double amt = leg.getPremium() * leg.getQuantity()
								* (Quote.BUY.equals(q.getBuyOrSell())   ? -1 : +1)
								* (StrategyLeg.LONG.equals(leg.getLs()) ? +1 : -1);
					
					double pos = leg.getQuantity()
							* (Quote.SELL.equals(q.getBuyOrSell())   ? -1 : +1)
							* (StrategyLeg.LONG.equals(leg.getLs())  ? +1 : -1);
					
					b.balance += amt;
					b.position += pos;
				}
			}
			keys = new ArrayList<PnlKey>(pnl.keySet());
			Collections.sort(keys);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public int getColumnCount() {
		return colms.length;
	}
	
	public String getColumnName(int column) {
		if(column < colms.length)
			return colms[column];
		return super.getColumnName(column);
	}

	public int getRowCount() {
		return keys.size();
	}

	public Object getValueAt(int r, int c) {
		PnlKey key = keys.get(r);
		Balance b = pnl.get(key);
		switch(c){
		case 0:
			return key.underlyingCode;
		case 1:
			return key.callOrPut;
		case 2:
			return key.strike;
		case 3:
			return key.expiry;
		case 4:
			return b.position;
		case 5:
			return b.balance;
		}
		return null;
	}

}
