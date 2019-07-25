package com.tamageta.financial.booking.dao;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.tamageta.financial.booking.rfq.data.Allocation;
import com.tamageta.financial.booking.rfq.data.AllocationAccount;
import com.tamageta.financial.booking.rfq.data.Client;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.Ric;
import com.tamageta.financial.booking.rfq.data.Underlying;

public class CacheDao implements Dao {
	private final Dao sourceDao;
	private Quote[] quoteCache = new Quote[1000];
	private Underlying[] underlyingCache = new Underlying[1000];
	private final Hashtable<String,Underlying> htUCache = new Hashtable<String,Underlying>();
	
	public CacheDao(Dao sourceDao){
		this.sourceDao = sourceDao; 
	}

	public void addUpdateListener(UpdateListener listener){
		sourceDao.addUpdateListener(listener);
	}
	public void removeUpdateListener(UpdateListener listener){
		sourceDao.removeUpdateListener(listener);
	}
	

    public String getUserId(){
    	return sourceDao.getUserId();
    }

    public boolean isLogin(){
    	return sourceDao.isLogin();
    }

	public synchronized Quote getQuote(long id) throws DaoException {
		int size = quoteCache.length;
		int index = (int)id%size;
		if(quoteCache[index] != null && quoteCache[index].getId() == id){
			return quoteCache[index];
		}
		quoteCache[index] = sourceDao.getQuote(id);
		return quoteCache[index];
	}
	
	public List<Long> getQuoteIds() throws DaoException {
		final List<Long> ids = sourceDao.getQuoteIds();
		
		Thread t = new Thread(){
			public void run(){
				int size = quoteCache.length;
				for(int i = 0; i < ids.size() && i < size; i++){
					long id = ids.get(i);
					int index = (int)(id%(long)size);
					if(quoteCache[index] != null && quoteCache[index].getId() == id){
						continue;
					}
					try{
						quoteCache[index] = sourceDao.getQuote(id);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
		return ids;
	}
	public Enumeration<Quote> getQuotes() throws DaoException{
		return new Enumeration<Quote>(){
			final Enumeration<Long> en = new Vector<Long>(getQuoteIds()).elements();
			
			public boolean hasMoreElements() {
				return en.hasMoreElements();
			}
			public Quote nextElement() {
				try{	
					return getQuote(en.nextElement());
				}catch(Exception e){
					e.printStackTrace();
					return null;
				}
			}
		};
	}
	public List<Ric> getUnderlyingRics(String key) throws DaoException{
		return sourceDao.getUnderlyingRics(key);
	}
	
	public List<String> getRicCodes(String key) throws DaoException{
		final List<String> rics = sourceDao.getRicCodes(key);
		Thread t = new Thread(){
			public void run(){
				int size = quoteCache.length;
				for(int i = 0; i < rics.size() && i < size; i++){
					try{
						getUnderlying(rics.get(i));
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
		return rics;
	}
	
	public synchronized Underlying getUnderlying(String ric) throws DaoException {
		if(ric == null || ric.trim().length() < 1){
			return null;
		}
		if(htUCache.containsKey(ric)){
			return htUCache.get(ric);
		}
		Underlying u = sourceDao.getUnderlying(ric);
		htUCache.put(ric, u);
		return u;
	}
	
	public AllocationAccount getAccount(String account) throws DaoException{
		return sourceDao.getAccount(account);
	}
	
	public List<AllocationAccount> getAccounts() throws DaoException {
		return sourceDao.getAccounts();
	}
	
	public void saveAccount(List<AllocationAccount> list) throws DaoException{
		sourceDao.saveAccount(list);
	}

	public List<Allocation> getAllocations(Quote quote) throws DaoException{
		return sourceDao.getAllocations(quote);
	}
	
	public void saveAllocations(List<Allocation> allocs) throws DaoException{
		sourceDao.saveAllocations(allocs);
	}
	
	public void sendQuote(Quote quote, String status) throws DaoException {
		sourceDao.sendQuote(quote, status);
	}

	public void login(String userId, char[] password) throws DaoException {
		sourceDao.login(userId, password);
	}

	public Quote createNewQuote(){
		return sourceDao.createNewQuote();
	}
	
	public void takeOwnership(Quote quote) throws DaoException {
		sourceDao.takeOwnership(quote);
	}
	public void releaseOwnership(Quote quote) throws DaoException {
		sourceDao.releaseOwnership(quote);
	}
	public double getDefaultRiskFreeRate() throws DaoException{
		return sourceDao.getDefaultRiskFreeRate();
	}
	public void changeStatus(Quote quote, String newStatus) throws DaoException{
		sourceDao.changeStatus(quote, newStatus);
	}
	public Quote createCloseQuote(Quote quote) throws DaoException{
		return sourceDao.createCloseQuote(quote);
	}
	public Quote copyQuote(Quote quote) throws DaoException{
		return sourceDao.copyQuote(quote);
	}
	public List<Client> getClients() throws DaoException{
		return sourceDao.getClients();
	}
	public List<Client> getClients(String name) throws DaoException{
		return sourceDao.getClients(name);
	}
	public Client getClient(String clientId) throws DaoException{
		return sourceDao.getClient(clientId);
	}
}
