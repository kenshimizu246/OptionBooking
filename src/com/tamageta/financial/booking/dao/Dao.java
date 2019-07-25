package com.tamageta.financial.booking.dao;

import java.util.Enumeration;
import java.util.List;

import com.tamageta.financial.booking.rfq.data.Allocation;
import com.tamageta.financial.booking.rfq.data.AllocationAccount;
import com.tamageta.financial.booking.rfq.data.Client;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.Ric;
import com.tamageta.financial.booking.rfq.data.Underlying;

public interface Dao {
    public String getUserId();
    public boolean isLogin();
	public void login(String userId, char[] password) throws DaoException;
	public List<String> getRicCodes(String key) throws DaoException;
	public List<Ric> getUnderlyingRics(String key) throws DaoException;
	public Underlying getUnderlying(String ric) throws DaoException;
	public void sendQuote(Quote quote, String status) throws DaoException;
	public List<Long> getQuoteIds() throws DaoException;
	public Quote getQuote(long id) throws DaoException;
	public Quote createNewQuote();
	public Enumeration<Quote> getQuotes() throws DaoException;
	public void addUpdateListener(UpdateListener listener);
	public void removeUpdateListener(UpdateListener listener);
	public void takeOwnership(Quote quote) throws DaoException;
	public void releaseOwnership(Quote quote) throws DaoException;
	public double getDefaultRiskFreeRate() throws DaoException;
	public void changeStatus(Quote quote, String newStatus) throws DaoException;
	public Quote createCloseQuote(Quote quote) throws DaoException;
	public Quote copyQuote(Quote quote) throws DaoException;

	public List<Client> getClients() throws DaoException;
	public List<Client> getClients(String name) throws DaoException;
	public Client getClient(String clientId) throws DaoException;
	public AllocationAccount getAccount(String account) throws DaoException;
	public List<AllocationAccount> getAccounts() throws DaoException;
	public void saveAccount(List<AllocationAccount> list) throws DaoException;
	public List<Allocation> getAllocations(Quote quote) throws DaoException;
	public void saveAllocations(List<Allocation> allocs) throws DaoException;
}