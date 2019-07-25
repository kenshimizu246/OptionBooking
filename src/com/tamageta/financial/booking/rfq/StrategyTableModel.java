package com.tamageta.financial.booking.rfq;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import com.tamageta.financial.booking.Environment;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.StrategyLeg;

public class StrategyTableModel extends AbstractTableModel {
	private String strategy = Quote.STRATEGY_CUSTOM;
	private Quote rfq;
	private List<StrategyLeg> legs;
	
	public static final int IDX_ID = 0;
	public static final int IDX_LS = 1;
	public static final int IDX_CP = 2;
	public static final int IDX_QTY = 3;
	public static final int IDX_STRIKE = 4;
	public static final int IDX_EXPIRY = 5;
	public static final int IDX_VOL    = 6;
	public static final int IDX_PREM   = 7;
	public static final int IDX_DELTA  = 8;
	public static final int IDX_GAMMA  = 9;
	public static final int IDX_VEGA   = 10;
	public static final int IDX_THETA  = 11;
	public static final int IDX_RHO    = 12;
	public static final int IDX_VOLGA  = 13;
	public static final int IDX_VANNA  = 14;
	public static final int IDX_CHARM  = 15;
	public static final int IDX_COLOR  = 16;
	public static final int IDX_DUAL_DELTA  = 17;
	public static final int IDX_3DUAL_GAMMA  = 18;
	
	public static final String[] columnNames = new String[]{
		"ID",
		"L/S",
		"C/P",
		"Qty",
		"Strike",
		"Expiry",
		"Vol",
		"Premium",
		"Delta",
		"Gamma",
		"Vega",
		"Theta",
		"Rho",
		"Volga",
		"Vanna",
		"Charm",
		"Color",
		"Dual Delta",
		"Dual Gamma"
	};

	public StrategyTableModel(){
		super();
	}
	
	public void setStrategy(String strategy){
		if(this.strategy.equals(strategy)){
			return;
		}
		this.strategy = strategy;
		fireTableDataChanged();
	}
	
	public void setRFQ(Quote rfq){
		this.rfq  = rfq;
		if(rfq != null){
			this.legs = rfq.getStrategies();
		}else{
			this.legs = null;
		}
		fireTableDataChanged();
	}
	
	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int c) {
		return columnNames[c];
	}

	public int getRowCount() {
		return (legs != null ? legs.size() : 0);
	}

    public Class getColumnClass(int c) {
        return (getValueAt(0, c) != null ? getValueAt(0, c).getClass() : String.class);
    }

	public Object getValueAt(int r, int c) {
		if(legs == null || legs.size() <= r){
			return null;
		}
		StrategyLeg leg = legs.get(r);
		switch (c) {
		case IDX_ID:
			return leg.getLegId();
		case IDX_LS:
			return leg.getLs();
		case IDX_CP:
			return leg.getCallOrPut();
		case IDX_QTY:
			return leg.getQuantity();
		case IDX_STRIKE:
			return leg.getStrike();
		case IDX_EXPIRY:
			return leg.getExpiry();
		case IDX_VOL:
			return leg.getVolatility();
		case IDX_PREM:
			return leg.getPremium();
		case IDX_DELTA:
			return leg.getDelta();
		case IDX_GAMMA:
			return leg.getGamma();
		case IDX_VEGA:
			return leg.getVega();
		case IDX_THETA:
			return leg.getTheta();
		case IDX_RHO:
			return leg.getRho();
		case IDX_VOLGA:
			return leg.getVolga();
		case IDX_VANNA:
			return leg.getVanna();
		case IDX_CHARM:
			return leg.getCharm();
		case IDX_COLOR:
			return leg.getColor();
		case IDX_DUAL_DELTA:
			return leg.getDualDelta();
		case IDX_3DUAL_GAMMA:
			return leg.getDualGamma();
		default:
			return null;
		}
	}

	public boolean isCellEditable(int r, int c) {
		switch (c) {
		case IDX_ID:
			return false;
		case IDX_LS:
			if(rfq != null 
				&& Quote.STATUS_ANALYSIS.equals(rfq.getStatus())
				&& Quote.STRATEGY_CUSTOM.equals(strategy)){
				return true;
			}
			return rfq != null 
					&& !rfq.hasOriginalQuoteId()
					&& Quote.STATUS_NEW.equals(rfq.getStatus())
					&& Quote.STRATEGY_CUSTOM.equals(strategy);
		case IDX_CP:
			if(rfq != null 
				&& Quote.STATUS_ANALYSIS.equals(rfq.getStatus())
				&& Quote.STRATEGY_CUSTOM.equals(strategy)){
				return true;
			}
			return rfq != null 
					&& !rfq.hasOriginalQuoteId()
					&& Quote.STATUS_NEW.equals(rfq.getStatus())
					&& Quote.STRATEGY_CUSTOM.equals(strategy);
		case IDX_QTY:  // QTY
			if(rfq != null
					&& (Quote.STATUS_NEW.equals(rfq.getStatus())
							|| Quote.STATUS_ANALYSIS.equals(rfq.getStatus()))){
				if(Quote.STRATEGY_CUSTOM.equals(strategy)){
					return true;
				}else if(Quote.STRATEGY_CALL_SPREAD.equals(strategy)
						|| Quote.STRATEGY_PUT_SPREAD.equals(strategy)
						|| Quote.STRATEGY_COLLAR.equals(strategy)
						|| Quote.STRATEGY_STRADDLE.equals(strategy)
						|| Quote.STRATEGY_STRANGLE.equals(strategy)
						|| Quote.STRATEGY_BUTTERFLY.equals(strategy)
						|| Quote.STRATEGY_CALENDAR.equals(strategy)
						|| Quote.STRATEGY_COVERED_WRITE.equals(strategy)
						){
					return (r == 0);
				}
			}
			return false;
		case IDX_STRIKE:  // Strike
			if(rfq != null
				&& (Quote.STATUS_NEW.equals(rfq.getStatus())
					|| Quote.STATUS_ANALYSIS.equals(rfq.getStatus()))){
				if(Quote.STRATEGY_CUSTOM.equals(strategy)
						|| Quote.STRATEGY_CALL_SPREAD.equals(strategy)
						|| Quote.STRATEGY_PUT_SPREAD.equals(strategy)
						|| Quote.STRATEGY_COLLAR.equals(strategy)
						|| Quote.STRATEGY_STRANGLE.equals(strategy)
						|| Quote.STRATEGY_BUTTERFLY.equals(strategy)
						){
					return true;
				}else if(Quote.STRATEGY_STRADDLE.equals(strategy)
						|| Quote.STRATEGY_CALENDAR.equals(strategy)
						|| Quote.STRATEGY_COVERED_WRITE.equals(strategy)
						){
					return (r == 0);
				}
			}
			return false;
		case IDX_EXPIRY:  // Expiry
			if(rfq != null
				&& (Quote.STATUS_NEW.equals(rfq.getStatus())
					|| Quote.STATUS_ANALYSIS.equals(rfq.getStatus()))){
				if(Quote.STRATEGY_CUSTOM.equals(strategy)
						|| Quote.STRATEGY_CALENDAR.equals(strategy)
						){
					return true;
				}else if(Quote.STRATEGY_CALL_SPREAD.equals(strategy)
						|| Quote.STRATEGY_PUT_SPREAD.equals(strategy)
						|| Quote.STRATEGY_COLLAR.equals(strategy)
						|| Quote.STRATEGY_STRADDLE.equals(strategy)
						|| Quote.STRATEGY_STRANGLE.equals(strategy)
						|| Quote.STRATEGY_BUTTERFLY.equals(strategy)
						|| Quote.STRATEGY_COVERED_WRITE.equals(strategy)
						){
					return (r == 0);
				}
			}
			return false;
		case IDX_VOL:  // Vol
		case IDX_PREM:  // Premium
			if(rfq == null || Quote.STATUS_ANALYSIS.equals(rfq.getStatus())){
				return true;
			}
			return rfq != null 
					&& Quote.STATUS_IOI.equals(rfq.getStatus())
					&& rfq.hasTrader()
					&& rfq.getTrader().equals(Environment.getUserID())
					;
		default:
			return false;
		}
	}

	public void setValueAt(Object val, int r, int c) {
		if(legs == null || legs.size() <= r){
	        fireTableCellUpdated(r, c);
			return;
		}
		StrategyLeg leg = legs.get(r);
		switch (c) {
		case IDX_LS:
			leg.setLs(val.toString());
			break;
		case IDX_CP:
			leg.setCallOrPut(val.toString());
			break;
		case IDX_QTY:
			if(val instanceof Number){
				leg.setQuantity(((Number)val).doubleValue());
				leg.doCalc();
			}else if(val instanceof String){
				leg.setQuantity(Double.parseDouble((String)val));
				leg.doCalc();
			}
			if(Quote.STRATEGY_CALL_SPREAD.equals(strategy)
					|| Quote.STRATEGY_PUT_SPREAD.equals(strategy)){
				legs.get(1).setQuantity(leg.getQuantity());
			}
			if(Quote.STRATEGY_CALL_SPREAD.equals(strategy)
					|| Quote.STRATEGY_PUT_SPREAD.equals(strategy)
					|| Quote.STRATEGY_COLLAR.equals(strategy)
					|| Quote.STRATEGY_STRADDLE.equals(strategy)
					|| Quote.STRATEGY_STRANGLE.equals(strategy)
					|| Quote.STRATEGY_CALENDAR.equals(strategy)
					|| Quote.STRATEGY_COVERED_WRITE.equals(strategy)
					){
				legs.get(1).setQuantity(leg.getQuantity());
				legs.get(1).doCalc();
				fireTableCellUpdated(1, c);
		}else if(Quote.STRATEGY_BUTTERFLY.equals(strategy)){
				legs.get(1).setQuantity(leg.getQuantity()*2);
				legs.get(2).setQuantity(leg.getQuantity());
				legs.get(1).doCalc();
				legs.get(2).doCalc();
				fireTableCellUpdated(1, c);
				fireTableCellUpdated(2, c);
			}
			break;
		case IDX_STRIKE:
			if(val instanceof Number){
				leg.setStrike(((Number)val).doubleValue());
				leg.doCalc();
			}else if(val instanceof String){
				leg.setStrike(Double.parseDouble((String)val));
				leg.doCalc();
			}
			if(Quote.STRATEGY_STRADDLE.equals(strategy)
					|| Quote.STRATEGY_CALENDAR.equals(strategy)
					|| Quote.STRATEGY_COVERED_WRITE.equals(strategy)
					){
				legs.get(1).setStrike(leg.getStrike());
				legs.get(1).doCalc();
				fireTableCellUpdated(1, c);
			}
			break;
		case IDX_EXPIRY:
			if(val instanceof Date){
				leg.setExpiry((Date)val);
				leg.doCalc();
			}
			if(Quote.STRATEGY_CALL_SPREAD.equals(strategy)
					|| Quote.STRATEGY_PUT_SPREAD.equals(strategy)
					|| Quote.STRATEGY_COLLAR.equals(strategy)
					|| Quote.STRATEGY_STRADDLE.equals(strategy)
					|| Quote.STRATEGY_STRANGLE.equals(strategy)
					|| Quote.STRATEGY_COVERED_WRITE.equals(strategy)
					){
				legs.get(1).setExpiry(leg.getExpiry());
				legs.get(1).doCalc();
				fireTableCellUpdated(1, c);
			}else if(Quote.STRATEGY_BUTTERFLY.equals(strategy)){
				legs.get(1).setExpiry(leg.getExpiry());
				legs.get(2).setExpiry(leg.getExpiry());
				legs.get(1).doCalc();
				legs.get(2).doCalc();
				fireTableCellUpdated(1, c);
				fireTableCellUpdated(2, c);
			}
			break;
		case IDX_VOL:  // Vol
			if(val instanceof Number){
				leg.setVolatility(((Number)val).doubleValue());
			}else if(val instanceof String){
				leg.setVolatility(Double.parseDouble((String)val));
			}
			leg.doCalc();
			fireTableRowsUpdated(r, r);
			if(Quote.STRATEGY_STRADDLE.equals(strategy)
					|| Quote.STRATEGY_CALENDAR.equals(strategy)
					|| Quote.STRATEGY_COVERED_WRITE.equals(strategy)
					){
				if(legs.size() < 2){
					// ERROR : must implement error 
					break;
				}
				legs.get(1).setVolatility(leg.getVolatility());
				legs.get(1).doCalc();
				fireTableCellUpdated(1, c);
			}
			break;
		case IDX_PREM:  // Premium
			if(val instanceof Number){
				leg.setPremium(((Number)val).doubleValue());
			}else if(val instanceof String){
				leg.setPremium(Double.parseDouble((String)val));
			}
			leg.setByPremium(true);
			leg.doCalc();
			fireTableRowsUpdated(r, r);
			if(Quote.STRATEGY_STRADDLE.equals(strategy)
					|| Quote.STRATEGY_CALENDAR.equals(strategy)
					|| Quote.STRATEGY_COVERED_WRITE.equals(strategy)
					){
				legs.get(1).setPremium(leg.getPremium());
				leg.setByPremium(true);
				legs.get(1).doCalc();
				fireTableCellUpdated(1, c);
			}
			break;
		}
        fireTableCellUpdated(r, c);
	}
	public StrategyLeg getStrategyLeg(int idx){
		return legs.get(idx);
	}
}
