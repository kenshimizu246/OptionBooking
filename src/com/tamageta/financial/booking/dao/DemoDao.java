package com.tamageta.financial.booking.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.xml.soap.SOAPException;

import com.tamageta.financial.booking.rfq.data.Allocation;
import com.tamageta.financial.booking.rfq.data.AllocationAccount;
import com.tamageta.financial.booking.rfq.data.Client;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.QuoteImpl;
import com.tamageta.financial.booking.rfq.data.Ric;
import com.tamageta.financial.booking.rfq.data.Underlying;

public class DemoDao implements Dao {
	private final Vector<Quote> quotes = new Vector<Quote>();
	private final Map<String,Underlying> underlyings = new HashMap<String, Underlying>();
	private final Map<String,Ric> rics = new HashMap<String, Ric>();
	private int quoteSequence = 1;
	private String loginId = null;

	private static List<Client> clients = new Vector<Client>();
	static{
		clients.add(new Client(1,"C1", "Client One", "active"));	
		clients.add(new Client(2,"C2", "Client Two", "active"));	
		clients.add(new Client(3,"C3", "Client Three", "active"));	
		clients.add(new Client(4,"C4", "Client Four", "active"));
		clients.add(new Client(5,"C5", "Client Five", "active"));
	}

	private static List<AllocationAccount> accts = new Vector<AllocationAccount>();
	static{
		accts.add(new AllocationAccount("AC1", "Account One"));	
		accts.add(new AllocationAccount("AC2", "Account Two"));	
		accts.add(new AllocationAccount("AC3", "Account Three"));	
		accts.add(new AllocationAccount("AC4", "Account Four"));
		accts.add(new AllocationAccount("AC5", "Account Five"));
	}
	
	public DemoDao(){
		InputStream in = null;
		try{
			in = DemoDao.class.getResourceAsStream("ric.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String s = null;
			while((s = br.readLine()) != null){
				String code = "";
				String name = "";
				String mic = "";
				int multipler = 0;
				
				if(s.indexOf("\t")>0){
					code = s.substring(0,s.indexOf("\t"));
					s = s.substring(s.indexOf("\t")+1);
				}
				if(s.indexOf("\t")>0){
					name = s.substring(0,s.indexOf("\t"));
					s = s.substring(s.indexOf("\t"));
				}
				if(s.indexOf("\t")>0){
					mic = s.substring(0,s.indexOf("\t"));
					s = s.substring(s.indexOf("\t"));
				}
				if(s.indexOf("\t")>0){
					multipler = Integer.parseInt(s.substring(0,s.indexOf("\t")));
				}
				System.out.println(code);
				Underlying ul = new Underlying(code, name, mic, multipler);
				ul.setPriceClose(100);
				underlyings.put(code, ul);
				rics.put(code, new Ric(code, name));
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
    public String getUserId(){
    	return loginId;
    }
	
    public boolean isLogin(){
    	return true;
    }
    
	public Quote getQuote(long id) throws DaoException {
		return quotes.get((int)id);
	}

	public List<Long> getQuoteIds() throws DaoException {
		List<Long> list = new ArrayList<Long>();
		for(int i = 0; i < quotes.size(); i++){
			list.add(new Long(i));
		}
		return list;
	}
	
	public Enumeration<Quote> getQuotes() throws DaoException {
		return quotes.elements();
	}
	
	public Quote createNewQuote(){
		Quote quote = new QuoteImpl(){
			
		};
		return quote;
	}

	public List<String> getRicCodes(String key) throws DaoException {
		List<String> list = new ArrayList<String>();
		for(String k : rics.keySet()){
			if(k.matches(key)){
				list.add(k);
			}
		}
		return list;
	}

	public Underlying getUnderlying(String ric) throws DaoException {
		return underlyings.get(ric);
	}

	public List<Ric> getUnderlyingRics(String key) throws DaoException {
		List<Ric> list = new ArrayList<Ric>();
		for(Entry<String, Ric> e : rics.entrySet()){
			if(key == null || e.getKey().matches(key)){
				list.add(e.getValue());
			}else if(e.getValue().getName().toUpperCase().indexOf(key.toUpperCase()) >= 0){
				list.add(e.getValue());
			}
		}
		return list;
	}

	public AllocationAccount getAccount(String account) throws DaoException{
		for(AllocationAccount acct : accts){
			if(acct.getAccount().equals(account)){
				return acct;
			}
		}
		return null;
	}
	
	public List<AllocationAccount> getAccounts() throws DaoException {
		return accts;
	}

	public void saveAccount(List<AllocationAccount> list) throws DaoException{
		accts.clear();
		for(AllocationAccount acct : list){
			accts.add(acct);
		}
	}

	public List<Allocation> getAllocations(Quote quote) throws DaoException{
		return new ArrayList<Allocation>();
	}
	
	public void saveAllocations(List<Allocation> allocs) throws DaoException{
		;
	}
	
	public void sendQuote(Quote quote, String status) throws DaoException {
		quote.setStatus(status);
		if(quote.getId() < 1){
			quote.setId(quoteSequence++);
		}
		if(!quotes.contains(quote)){
			quotes.add(quote);
		}
		try{
			Thread.sleep(1000);
		}catch(Exception e){
			;
		}
	}
	public void login(String userId, char[] password){
		loginId = userId;
	}
	public void addUpdateListener(UpdateListener listener){
		
	}
	public void removeUpdateListener(UpdateListener listener){
		
	}
	public void takeOwnership(Quote quote){
		
	}
	public void releaseOwnership(Quote quote){
		
	}
	public double getDefaultRiskFreeRate() throws DaoException{
		return 0.0012;
	}
	public void changeStatus(Quote quote, String newStatus) throws DaoException{
		quote.setStatus(newStatus);
	}
	public Quote createCloseQuote(Quote quote) throws DaoException{
		return null;
	}
	public Quote copyQuote(Quote quote) throws DaoException{
		Quote q = new QuoteImpl(){
			
		};
		return q;
	}
	public List<Client> getClients() throws DaoException{
		return clients;
	}
	public List<Client> getClients(String name) throws DaoException{
		ArrayList<Client> cc = new ArrayList<Client>();
		for(Client client : clients){
			if(client.getName().matches(name)){
				cc.add(client);
			}
		}
		return cc;
	}
	public Client getClient(String clientId) throws DaoException{
		for(Client client : clients){
			if(client.getClientId().equals(clientId)){
				return client;
			}
		}
		return null;
	}
}