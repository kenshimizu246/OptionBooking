package com.tamageta.financial.booking.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.tamageta.financial.booking.dao.CacheDao;
import com.tamageta.financial.booking.dao.Dao;
import com.tamageta.financial.booking.dao.DaoException;
import com.tamageta.financial.booking.dao.SoapDao;
import com.tamageta.financial.booking.dao.UpdateEvent;
import com.tamageta.financial.booking.dao.UpdateListener;
import com.tamageta.financial.booking.rfq.data.Quote;

public class TradeTableModel extends AbstractTableModel implements UpdateListener {
	private Dao dao = null;
	private List<Long> trades = new Vector<Long>();
	private static final String[] columnNames = new String[]{
		"ID",
		"QuoteID",
		"Status",
		"Trade Date",
		"UserID",
		"TraderID",
		"ClientID",
		"Client Name",
		"Buy/Sell",
		"Close/Open",
		"Underlying RIC",
		"Underlying Name",
		"Underlying MIC",
		"Spot Price",
		"Intest Rate",
		"Settl' Date",
		"Settl' CCY"
	};
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public TradeTableModel(){

	}

	public void updated(UpdateEvent event) {
		System.out.println("TradeTableModel.update:"+event);
		if(UpdateEvent.TYPE_QUOTE == event.getObjectType()){
			int row = trades.indexOf(event.getObjectId());
			if(row >= 0){
				fireTableRowsUpdated(row, row);
			}else{
				try{
					Quote newQuote = dao.getQuote(event.getObjectId());
					for(int i = 0; i < trades.size(); i++){
						long id = trades.get(i);
						Quote qq = dao.getQuote(id);
						if(newQuote.getQuoteId().equals(qq.getQuoteId())){
							trades.set(i, newQuote.getId());
							System.out.println("found!");
							fireTableRowsUpdated(i, i);
							return;
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				try {
					System.out.println("updateList!");
					updateList();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public int getRowIndex(long objectId){
		return trades.indexOf(objectId);
	}

	public void setDao(Dao dao)throws Exception {
		this.dao = dao;
		dao.addUpdateListener(this);
		updateList();
	}
	
	public void updateList()throws Exception {
		trades = dao.getQuoteIds();
		fireTableDataChanged();
	}
	
	public int getIndexByTradeId(String tradeId)throws DaoException{
		for(int i = 0; i < trades.size(); i++){
			Quote q = dao.getQuote(trades.get(i));
			if(tradeId.equals(q.getQuoteId())){
				return i;
			}
		}
		return -1;
	}
	
	public String getColumnName(int arg0) {
		return columnNames[arg0];
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return (trades != null ? trades.size() : 0);
	}

	public Object getValueAt(int r, int c) {
		if(dao == null){
			return null;
		}
		try{
			Quote q = dao.getQuote(trades.get(r));
			switch(c){
			case 0:
				return q.getId();
			case 1:
				return q.getQuoteId();
			case 2:
				return q.getStatus();
			case 3:
				return df.format(q.getTradeDate());
			case 4:
				return q.getUserId();
			case 5:
				return q.getTrader();
			case 6:
				return q.getClientId();
			case 7:
				return q.getClientName();
			case 8:
				return q.getBuyOrSell();
			case 9:
				return q.getOpenClose();
			case 10:
				return q.getUnderlyingCode();
			case 11:
				return q.getUnderlyingName();
			case 12:
				return q.getUnderlyingPrimaryMIC();
			case 13:
				return q.getSpotPrice();
			case 14:
				return q.getRiskFreeRate();
			case 15:
				return q.getPremiumSettlement();
			case 16:
				return q.getPremiumCcy();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public Quote getRow(int row)throws Exception{
		if(dao == null){
			return null;
		}
		return (0 <= row && row < trades.size() ? dao.getQuote(trades.get(row)) : null);
	}
	
	@Deprecated
	public Quote getQuote(String quoteId)throws Exception{
		if(dao == null){
			return null;
		}
		for(long id : trades){
			Quote q = dao.getQuote(id);
			if(quoteId.equals(q.getQuoteId())){
				return q;
			}
		}
		return null;
	}
}
