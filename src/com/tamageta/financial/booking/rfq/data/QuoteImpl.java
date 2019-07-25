package com.tamageta.financial.booking.rfq.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuoteImpl extends DataUpdateNotifiableImpl implements Serializable, Quote{
	public static final String STATUS_NEW = "New";
	public static final String STATUS_IOI = "IOI";
	public static final String STATUS_QUOTE = "Quote";
	public static final String STATUS_AFFIRM = "Affirm";
	public static final String STATUS_DONE = "Done";
	public static final String STATUS_CANCEL = "Cancel";
	public static final String INT_STATUS_HEDGE_REQ  = "Hedge Req";
	public static final String INT_STATUS_HEDGE_DONE = "Hedge Done";
	public static final String INT_STATUS_BOOKING_REQ = "Booking Req";
	public static final String INT_STATUS_BOOKED = "Booked";
	
	public static final String STRATEGY_CUSTOM      = "Custom";
	public static final String STRATEGY_CALL_SPREAD = "Call Spread";
	public static final String STRATEGY_PUT_SPREAD  = "Put Spread";
	public static final String STRATEGY_COLLAR      = "Collar";
	public static final String STRATEGY_STRADDLE    = "Straddle";
	public static final String STRATEGY_STRANGLE    = "Strangle";
	public static final String STRATEGY_BUTTERFLY   = "Butterfly";
	public static final String STRATEGY_CALENDAR    = "Calendar";
	public static final String STRATEGY_COVERED_WRITE = "Covered Write";
	public static final String[] STRATEGIES = new String[]{
		STRATEGY_CUSTOM,
		STRATEGY_CALL_SPREAD,
		STRATEGY_PUT_SPREAD,
		STRATEGY_COLLAR,
		STRATEGY_STRADDLE,
		STRATEGY_STRANGLE,
		STRATEGY_BUTTERFLY,
		STRATEGY_CALENDAR,
		STRATEGY_COVERED_WRITE
	};
	
	public static final String OPEN  = "Open";
	public static final String CLOSE = "Close";
	
	public static final String SELL = "Sell";
	public static final String BUY  = "Buy";

	private long id = -1;
	private String quoteId;
	private String status = STATUS_NEW;
	private Date tradeDate = new Date();
	private String userId;
	private String clientId;
	private String clientName;
	private String buyOrSell = SELL;
	private String openClose = OPEN;
	private String premiumSettlement = "3D";
	private String premiumCcy = "JPY";
	private String underlyingCode;
	private String underlyingName;
	private String underlyingPrimaryMIC;
	private double spotPrice;
	private double riskFreeRate;
	private String strategy = STRATEGY_CUSTOM;
	private String trader = null;
	private String originalQuoteId;
	
	private List<StrategyLeg> strategies = new ArrayList<StrategyLeg>();
	
	public QuoteImpl(){
		setNewQuoteId();
	}
	
	public void setNewQuoteId(){
		DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
		quoteId = df.format(new Date());
		fireDataUpdateHandler(new DataUpdateEvent(this, "quoteId"));
	}
	
	public String getBuyOrSell() {
		return buyOrSell;
	}
	public void setBuyOrSell(String buyOrSell) {
		this.buyOrSell = buyOrSell;
		fireDataUpdateHandler(new DataUpdateEvent(this, "buyOrSell"));
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
		fireDataUpdateHandler(new DataUpdateEvent(this, "clientId"));
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
		fireDataUpdateHandler(new DataUpdateEvent(this, "clientName"));
	}
	public String getPremiumCcy() {
		return premiumCcy;
	}
	public void setPremiumCcy(String premiumCcy) {
		this.premiumCcy = premiumCcy;
		fireDataUpdateHandler(new DataUpdateEvent(this, "premiumCcy"));
	}
	public String getPremiumSettlement() {
		return premiumSettlement;
	}
	public void setPremiumSettlement(String premiumSettlement) {
		this.premiumSettlement = premiumSettlement;
		fireDataUpdateHandler(new DataUpdateEvent(this, "premiumSettlement"));
	}
	public String getUnderlyingPrimaryMIC() {
		return underlyingPrimaryMIC;
	}
	public void setUnderlyingPrimaryMIC(String primaryMIC) {
		this.underlyingPrimaryMIC = primaryMIC;
		fireDataUpdateHandler(new DataUpdateEvent(this, "underlyingPrimaryMIC"));
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
		fireDataUpdateHandler(new DataUpdateEvent(this, "status"));
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
		fireDataUpdateHandler(new DataUpdateEvent(this, "userId"));
	}
	public String getUnderlyingCode() {
		return underlyingCode;
	}
	public void setUnderlyingCode(String underlyingCode) {
		this.underlyingCode = underlyingCode;
		fireDataUpdateHandler(new DataUpdateEvent(this, "underlyingCode"));
	}
	public String getUnderlyingName() {
		return underlyingName;
	}
	public void setUnderlyingName(String underlyingName) {
		this.underlyingName = underlyingName;
		fireDataUpdateHandler(new DataUpdateEvent(this, "underlyingName"));
	}
	public void setTradeDate(Date tradeDate) {
		this.tradeDate = tradeDate;
		fireDataUpdateHandler(new DataUpdateEvent(this, "tradeDate"));
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
		fireDataUpdateHandler(new DataUpdateEvent(this, "id"));
	}
	public Date getTradeDate() {
		return tradeDate;
	}

	public List<StrategyLeg> getStrategies() {
		return strategies;
	}
	public void addStrategy(StrategyLeg leg){
		strategies.add(leg);
		fireDataUpdateHandler(new DataUpdateEvent(this, "strategies"));
	}
	public void removeStrategy(StrategyLeg leg){
		strategies.remove(leg);
		fireDataUpdateHandler(new DataUpdateEvent(this, "strategies"));
	}
	public void removeStrategy(int i){
		strategies.remove(i);
		fireDataUpdateHandler(new DataUpdateEvent(this, "strategies"));
	}
	public StrategyLeg addStrategyLeg(){
		StrategyLeg leg = new StrategyLeg(this);
		synchronized (strategies) {
			int max = 0;
			for(StrategyLeg l : strategies){
				max = Math.max(max, l.getLegId());
			}
			leg.setLegId(max+1);
			strategies.add(leg);
		}
		fireDataUpdateHandler(new DataUpdateEvent(this, "strategies"));
		return leg;
	}
	public StrategyLeg getStrategyLeg(long legId){
		for(StrategyLeg leg : strategies){
			if(leg.getLegId() == legId){
				return leg;
			}
		}
		return null;
	}
	public String getOpenClose() {
		return openClose;
	}
	public void setOpenClose(String openClose) {
		this.openClose = openClose;
		fireDataUpdateHandler(new DataUpdateEvent(this, "openClose"));
	}
	public double getSpotPrice() {
		return spotPrice;
	}
	public void setSpotPrice(double spotPrice) {
		this.spotPrice = spotPrice;
		fireDataUpdateHandler(new DataUpdateEvent(this, "spotPrice"));
	}
	public double getRiskFreeRate() {
		return riskFreeRate;
	}
	public void setRiskFreeRate(double riskFreeRate) {
		this.riskFreeRate = riskFreeRate;
		fireDataUpdateHandler(new DataUpdateEvent(this, "riskFreeRate"));
	}
	public String getStrategy() {
		return strategy;
	}
	public void setStrategy(String strategy) {
		if(strategy == null){
			throw new IllegalStateException();
		}
		if(strategy.equals(this.strategy)){
			return;
		}
		this.strategy = strategy;
		fireDataUpdateHandler(new DataUpdateEvent(this, "strategy"));
	}
	public void setStrategyLegs(String strategy) {
		setStrategy(strategy);
		
		if(STRATEGY_CALL_SPREAD.equals(strategy)
				|| STRATEGY_PUT_SPREAD.equals(strategy)){
			resetStrategyLeg(2);
			strategies.get(0).setCallOrPut(
					STRATEGY_CALL_SPREAD.equals(strategy)
					?StrategyLeg.CALL:StrategyLeg.PUT);
			strategies.get(1).setCallOrPut(
					STRATEGY_CALL_SPREAD.equals(strategy)
					?StrategyLeg.CALL:StrategyLeg.PUT);
			strategies.get(0).setLs(StrategyLeg.LONG);
			strategies.get(1).setLs(StrategyLeg.SHORT);
			strategies.get(1).setExpiry(strategies.get(0).getExpiry());
			strategies.get(1).setQuantity(strategies.get(0).getQuantity());
		}else if(STRATEGY_COLLAR.equals(strategy)){
			// Buy $3.50 put @ 8 cents
			// Sell $4.50 call @ 16 cents
			// Bought stock at $4.00
			resetStrategyLeg(2);
			strategies.get(0).setCallOrPut(StrategyLeg.PUT);
			strategies.get(0).setLs(StrategyLeg.SHORT);
			strategies.get(1).setCallOrPut(StrategyLeg.CALL);
			strategies.get(1).setLs(StrategyLeg.LONG);
			strategies.get(1).setExpiry(strategies.get(0).getExpiry());
			strategies.get(1).setQuantity(strategies.get(0).getQuantity());
		}else if(STRATEGY_STRADDLE.equals(strategy)){
			// Buy 1 Nov $5.00 Call @ $0.38 and 
			// Buy 1 Nov $5.00 Put @ $0.38
			resetStrategyLeg(2);
			strategies.get(0).setCallOrPut(StrategyLeg.CALL);
			strategies.get(0).setLs(StrategyLeg.LONG);
			strategies.get(1).setCallOrPut(StrategyLeg.PUT);
			strategies.get(1).setLs(StrategyLeg.LONG);
			strategies.get(1).setExpiry(strategies.get(0).getExpiry());
			strategies.get(1).setQuantity(strategies.get(0).getQuantity());
			strategies.get(1).setVolatility(strategies.get(0).getVolatility());
			strategies.get(1).setStrike(strategies.get(0).getStrike());
		}else if(STRATEGY_STRANGLE.equals(strategy)){
			// Buy 1 Feb $4.50 Call @ $0.19 and
			// Buy 1 Feb $3.50 Put @ $0.10
			resetStrategyLeg(2);
			strategies.get(0).setCallOrPut(StrategyLeg.CALL);
			strategies.get(0).setLs(StrategyLeg.LONG);
			strategies.get(1).setCallOrPut(StrategyLeg.PUT);
			strategies.get(1).setLs(StrategyLeg.LONG);
			strategies.get(1).setExpiry(strategies.get(0).getExpiry());
			strategies.get(1).setQuantity(strategies.get(0).getQuantity());
		}else if(STRATEGY_BUTTERFLY.equals(strategy)){
			//Buy 1 Feb $2.75 Call @ $0.45
			//Sell 2 Feb $3.00 Calls @ $0.27
			//Buy 1 Feb $3.25 Call @ $0.14
			resetStrategyLeg(3);
			strategies.get(0).setCallOrPut(StrategyLeg.CALL);
			strategies.get(0).setLs(StrategyLeg.LONG);
			strategies.get(1).setCallOrPut(StrategyLeg.CALL);
			strategies.get(1).setLs(StrategyLeg.SHORT);
			strategies.get(2).setCallOrPut(StrategyLeg.CALL);
			strategies.get(2).setLs(StrategyLeg.LONG);
			strategies.get(1).setExpiry(strategies.get(0).getExpiry());
			strategies.get(2).setExpiry(strategies.get(0).getExpiry());
			strategies.get(1).setQuantity(strategies.get(0).getQuantity() * 2);
			strategies.get(2).setQuantity(strategies.get(0).getQuantity());
		}else if(STRATEGY_CALENDAR.equals(strategy)){
			// Sell 1 Dec $4.00 Call @ $0.22 and 
			// Buy 1 Mar $4.00 Call @ $0.38
			resetStrategyLeg(2);
			strategies.get(0).setCallOrPut(StrategyLeg.CALL);
			strategies.get(0).setLs(StrategyLeg.SHORT);
			strategies.get(1).setCallOrPut(StrategyLeg.CALL);
			strategies.get(1).setLs(StrategyLeg.LONG);
			strategies.get(1).setQuantity(strategies.get(0).getQuantity());
			strategies.get(1).setStrike(strategies.get(0).getStrike());
		}else if(STRATEGY_COVERED_WRITE.equals(strategy)){
			resetStrategyLeg(1);
			strategies.get(0).setCallOrPut(StrategyLeg.CALL);
			strategies.get(0).setLs(StrategyLeg.SHORT);
		}
		fireDataUpdateHandler(new DataUpdateEvent(this, "strategy"));
	}
	private void resetStrategyLeg(int legSize){
		for(int i = 0; i < Math.max(strategies.size(),legSize); i++){
			if(i >= legSize){
				strategies.remove(i);
				continue;
			}else if(strategies.size() <= i){
				addStrategyLeg();
			}
		}
		fireDataUpdateHandler(new DataUpdateEvent(this, ""));
	}
	public String getQuoteId() {
		return quoteId;
	}
	public void setQuoteId(String quoteId) {
		this.quoteId = quoteId;
		fireDataUpdateHandler(new DataUpdateEvent(this, "quoteId"));
	}
	public boolean hasTrader(){
		return trader != null && trader.trim().length() > 0;
	}
	public void setTrader(String trader){
		this.trader = trader;
		fireDataUpdateHandler(new DataUpdateEvent(this, "trader"));
	}
	public String getTrader() {
		return trader;
	}
	public String getOriginalQuoteId() {
		return originalQuoteId;
	}
	public void setOriginalQuoteId(String originalQuoteId) {
		this.originalQuoteId = originalQuoteId;
		fireDataUpdateHandler(new DataUpdateEvent(this, "originalQuoteId"));
	}
	public boolean hasOriginalQuoteId(){
		return originalQuoteId != null;
	}
}
