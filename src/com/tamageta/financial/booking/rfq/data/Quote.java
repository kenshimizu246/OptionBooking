package com.tamageta.financial.booking.rfq.data;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface Quote extends DataUpdateNotifiable {
	public static final String STATUS_NEW = "New";
	public static final String STATUS_IOI = "IOI";
	public static final String STATUS_QUOTE = "Quote";
	public static final String STATUS_AFFIRM = "Affirm";
	public static final String STATUS_REJECT = "Reject";
	public static final String STATUS_HEDGE = "Hedge";
	public static final String STATUS_DONE = "Done";
	public static final String STATUS_CANCEL = "Cancel";
	public static final String STATUS_AMEND = "Amend";
	public static final String STATUS_ANALYSIS = "Analysis";
	public static final String INT_STATUS_HEDGE_REQ = "Hedge Req";
	public static final String INT_STATUS_HEDGE_DONE = "Hedge Done";
	public static final String INT_STATUS_BOOKING_REQ = "Booking Req";
	public static final String INT_STATUS_BOOKED = "Booked";
	public static final String STRATEGY_CUSTOM = "Custom";
	public static final String STRATEGY_CALL_SPREAD = "Call Spread";
	public static final String STRATEGY_PUT_SPREAD = "Put Spread";
	public static final String STRATEGY_COLLAR = "Collar";
	public static final String STRATEGY_STRADDLE = "Straddle";
	public static final String STRATEGY_STRANGLE = "Strangle";
	public static final String STRATEGY_BUTTERFLY = "Butterfly";
	public static final String STRATEGY_CALENDAR = "Calendar";
	public static final String STRATEGY_COVERED_WRITE = "Covered Write";
	public static final String[] STRATEGIES = new String[] { STRATEGY_CUSTOM,
			STRATEGY_CALL_SPREAD, STRATEGY_PUT_SPREAD, STRATEGY_COLLAR,
			STRATEGY_STRADDLE, STRATEGY_STRANGLE, STRATEGY_BUTTERFLY,
			STRATEGY_CALENDAR, STRATEGY_COVERED_WRITE };
	public static final String OPEN = "Open";
	public static final String CLOSE = "Close";
	public static final String SELL = "Sell";
	public static final String BUY = "Buy";

	public String getBuyOrSell();
	public void setBuyOrSell(String buyOrSell);
	public String getClientId();
	public void setClientId(String clientId);
	public String getClientName();
	public void setClientName(String clientName);
	public String getPremiumCcy();
	public void setPremiumCcy(String premiumCcy);
	public String getPremiumSettlement();
	public void setPremiumSettlement(String premiumSettlement);
	public String getUnderlyingPrimaryMIC();
	public void setUnderlyingPrimaryMIC(String primaryMIC);
	public String getStatus();
	public void setStatus(String status);
	public String getUserId();
	public void setUserId(String userId);
	public String getUnderlyingCode();
	public void setUnderlyingCode(String underlyingCode);
	public String getUnderlyingName();
	public void setUnderlyingName(String underlyingName);
	public void setTradeDate(Date tradeDate);
	public long getId();
	public void setId(long id);
	public Date getTradeDate();
	public List<StrategyLeg> getStrategies();
	public StrategyLeg getStrategyLeg(long legId);
	public void addStrategy(StrategyLeg leg);
	public void removeStrategy(StrategyLeg leg);
	public void removeStrategy(int i);
	public StrategyLeg addStrategyLeg();
	public String getOpenClose();
	public void setOpenClose(String openClose);
	public double getSpotPrice();
	public void setSpotPrice(double spotPrice);
	public double getRiskFreeRate();
	public void setRiskFreeRate(double riskFreeRate);
	public String getStrategy();
	public void setStrategy(String strategy);
	public void setStrategyLegs(String strategy);
	public String getQuoteId();
	public void setQuoteId(String quoteId);
	public boolean hasTrader();
	public void setTrader(String trader);
	public String getTrader();	
	public String getOriginalQuoteId();
	public void setOriginalQuoteId(String originalQuoteId);
	public void setNewQuoteId();
	public boolean hasOriginalQuoteId();
}
