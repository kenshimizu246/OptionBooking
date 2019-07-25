package com.tamageta.financial.booking.dao;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tamageta.financial.booking.rfq.data.Allocation;
import com.tamageta.financial.booking.rfq.data.AllocationAccount;
import com.tamageta.financial.booking.rfq.data.Client;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.Ric;
import com.tamageta.financial.booking.rfq.data.Underlying;

public class LRUCacheDao implements Dao , UpdateListener{
	private final Dao sourceDao;
	private Vector<Quote> quotes = new Vector<Quote>();
	private Vector<Underlying> underlyings = new Vector<Underlying>();
	private Vector<AllocationAccount> accounts = new Vector<AllocationAccount>();
	private Hashtable<Long,Quote> htQuotes = new Hashtable<Long,Quote>();
	private Hashtable<String,Underlying> htUnderlyings = new Hashtable<String,Underlying>();
	private Hashtable<String,AllocationAccount> htAccounts = new Hashtable<String,AllocationAccount>();
	private Vector<UpdateListener> listeners = new Vector<UpdateListener>();
	private final ExecutorService executor;
	
	private int quotesSize = 1000;
	private int underlyingSize = 1000;
	private int accountSize = 1000;
	
	public LRUCacheDao(Dao sourceDao){
		this.sourceDao = sourceDao; 
		this.sourceDao.addUpdateListener(this);
		this.executor = Executors.newFixedThreadPool(20);
	}
	public void preCache()throws DaoException {
		getQuoteIds();
	}
	public void updated(UpdateEvent event){
		System.out.println("UpdateEvent:"+event);
		if(UpdateEvent.TYPE_QUOTE == event.getObjectType()){
			long id = event.getObjectId();
			try{
				Quote q = sourceDao.getQuote(id);
				addQuote(q);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		for(UpdateListener l : listeners){
			l.updated(event);
		}
	}
	public void addUpdateListener(UpdateListener listener){
		Vector<UpdateListener> ll = (Vector<UpdateListener>)listeners.clone();
		ll.addElement(listener);
		listeners = ll;
	}
	public void removeUpdateListener(UpdateListener listener){
		Vector<UpdateListener> ll = (Vector<UpdateListener>)listeners.clone();
		ll.removeElement(listener);
		listeners = ll;
	}
	
    public String getUserId(){
    	return sourceDao.getUserId();
    }

    public boolean isLogin(){
    	return sourceDao.isLogin();
    }

	public synchronized Quote getQuote(long id) throws DaoException {
		if(htQuotes.containsKey(id)){
			return htQuotes.get(id);
		}
		Quote qq = sourceDao.getQuote(id);
		System.out.println("getQuote:"+id);
		addQuote(qq);
		return qq;
	}
	
	private synchronized void addQuote(Quote quote){
		Vector<Quote> qq = (Vector<Quote>)quotes.clone();
		if(qq.size() > quotesSize){
			Quote rr = qq.lastElement();
			qq.removeElement(rr);
			htQuotes.remove(rr.getId());
		}
		if(qq.contains(quote)){
			qq.removeElement(quote);
		}
		qq.insertElementAt(quote, 0);
		quotes = qq;
		Hashtable<Long,Quote> ht = (Hashtable<Long,Quote>)htQuotes.clone();
		ht.put(quote.getId(), quote);
		htQuotes = ht;
	}
	
	private synchronized void addUnderlying(Underlying u){
		Vector<Underlying> uu = (Vector<Underlying>)underlyings.clone();
		if(uu.size() > underlyingSize){
			Underlying rr = uu.lastElement();
			uu.removeElement(rr);
			htQuotes.remove(rr);
		}
		if(uu.contains(u)){
			uu.removeElement(u);
		}
		uu.insertElementAt(u, 0);
		underlyings = uu;
		if(!htUnderlyings.containsKey(u.getRic())){
			Hashtable<String,Underlying> ht = (Hashtable<String,Underlying>)htUnderlyings.clone();
			ht.put(u.getRic(), u);
			htUnderlyings = ht;
		}
	}
	
	private class QuoteFetch implements Runnable {
		private final long id;
		private QuoteFetch(long id){
			this.id = id;
		}
		public void run(){
			try{
				Quote quote = sourceDao.getQuote(id);
				if(quote != null){
					addQuote(quote);
					getUnderlying(quote.getUnderlyingCode());
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public List<Long> getQuoteIds() throws DaoException {
		final List<Long> ids = sourceDao.getQuoteIds();
		
		Thread t = new Thread(){
			public void run(){
				for(int i = 0; i < ids.size(); i++){
					long id = ids.get(i);
					
					if(!htQuotes.containsKey(id)){
						executor.execute(new QuoteFetch(id));
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
	
	private class UnderlyingFetch implements Runnable {
		private final String ric;
		private UnderlyingFetch(String ric){
			this.ric = ric;
		}
		public void run(){
			try{
				Underlying u = sourceDao.getUnderlying(ric);
				addUnderlying(u);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public List<String> getRicCodes(String key) throws DaoException{
		final List<String> rics = sourceDao.getRicCodes(key);

		Thread t = new Thread(){
			public void run(){
				for(String ric : rics){
					executor.execute(new UnderlyingFetch(ric));
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
		if(htUnderlyings.containsKey(ric)){
			return htUnderlyings.get(ric);
		}
		Underlying u = sourceDao.getUnderlying(ric);
		addUnderlying(u);
		return u;
	}
	
	private void addAccount(AllocationAccount account){
		Vector<AllocationAccount> aa = (Vector<AllocationAccount>)accounts.clone();
		if(aa.size() > accountSize){
			AllocationAccount a = aa.lastElement();
			aa.removeElement(a);
			htAccounts.remove(a);
		}
		if(aa.contains(account)){
			aa.removeElement(account);
		}
		aa.insertElementAt(account, 0);
		accounts = aa;
		if(!htAccounts.containsKey(account.getAccount())){
			Hashtable<String,AllocationAccount> ht = (Hashtable<String,AllocationAccount>)htAccounts.clone();
			ht.put(account.getAccount(), account);
			htAccounts = ht;
		}
	}
	public AllocationAccount getAccount(String account) throws DaoException{
		if(htAccounts.containsKey(account)){
			return htAccounts.get(account);
		}
		AllocationAccount a = sourceDao.getAccount(account);
		addAccount(a);
		return a;
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
