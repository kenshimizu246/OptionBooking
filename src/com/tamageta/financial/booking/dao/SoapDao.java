package com.tamageta.financial.booking.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.tamageta.financial.booking.rfq.data.Allocation;
import com.tamageta.financial.booking.rfq.data.AllocationAccount;
import com.tamageta.financial.booking.rfq.data.Client;
import com.tamageta.financial.booking.rfq.data.Price;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.QuoteImpl;
import com.tamageta.financial.booking.rfq.data.Ric;
import com.tamageta.financial.booking.rfq.data.StrategyLeg;
import com.tamageta.financial.booking.rfq.data.Underlying;;

public class SoapDao implements Dao {
    public static final String NAMESPACE_URI = "http://www.tamageta.com/spring-ws/samples/echo";
    public static final String PREFIX = "tns";
    private static final String INVALID_SESSIONID = "0";

    private SOAPConnectionFactory connectionFactory;
    private MessageFactory messageFactory;
    private final URL url;
    private volatile boolean stopEventPolling = false;
    private Vector<UpdateListener> quoteListeners = new Vector<UpdateListener>();
    
    private java.text.DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private java.text.DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private EventPolling eventPollingThread = null;
    private String sessionId = null;
    private String userId = null;

	private final XPath xpath;

	public static class QuoteDao extends QuoteImpl{
		private Quote createCopy() throws DaoException {
			Quote newQuote = null;
			ObjectOutputStream out;
			ObjectInputStream in;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try{
				out = new ObjectOutputStream(baos);
				out.writeObject(this);
				out.flush();
				out.close();
				
				in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
				newQuote = (Quote)in.readObject();
				newQuote.setId(-1);
				for(StrategyLeg leg : newQuote.getStrategies()){
					leg.setId(-1);
				}
				return newQuote;
			}catch(ClassNotFoundException e){
				e.printStackTrace();
				throw new DaoException(e);
			}catch(IOException e){
				e.printStackTrace();
				throw new DaoException(e);
			}
		}
	}
    
    public SoapDao(String url) throws SOAPException, MalformedURLException {
        connectionFactory = SOAPConnectionFactory.newInstance();
        messageFactory = MessageFactory.newInstance();
        
        this.url = new URL(url);
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		xpath.setNamespaceContext(new NamespaceContext(){
			public String getNamespaceURI(String prefix) {
				if (prefix == null)
			      throw new NullPointerException();
				return
				PREFIX.equals(prefix) ? NAMESPACE_URI :
		      "xml".equals(prefix) ? XMLConstants.XML_NS_URI :
		    	  XMLConstants.NULL_NS_URI;
			}
			public String getPrefix(String arg0) {
				return null;
			}
			public Iterator getPrefixes(String arg0) {
				return null;
			}
		});
        
    }
    
    private synchronized void startEventPolling(){
    	stopEventPolling();
    	eventPollingThread = new EventPolling();
    	eventPollingThread.start();
    }
    
    private synchronized void stopEventPolling(){
    	if(eventPollingThread != null){
    		stopEventPolling = true;
    		eventPollingThread.interrupt();
    		try{
    			eventPollingThread.join(500);
    		}catch(InterruptedException e){
    			e.printStackTrace();
    		}finally{
    			eventPollingThread = null;
        		stopEventPolling = false;
    		}
    	}
    }
    
    private class EventPolling extends Thread {
    	public void run(){
    		while(!stopEventPolling){
    			try{
    				checkEvent();
    				Thread.sleep(5000);
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    		}
    	}
    }
    
	public void addUpdateListener(UpdateListener listener){
		Vector<UpdateListener> ll = (Vector<UpdateListener>)quoteListeners.clone();
		ll.add(listener);
		quoteListeners = ll;
	}
	
	public void removeUpdateListener(UpdateListener listener){
		Vector<UpdateListener> ll = (Vector<UpdateListener>)quoteListeners.clone();
		ll.remove(listener);
		quoteListeners = ll;
	}
	
	private SOAPBodyElement getRequestBodyElement(SOAPMessage request, String requestName) throws SOAPException {
        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
        envelope.getHeader().addChildElement("sessionId", PREFIX, NAMESPACE_URI).setValue(sessionId);
        
        Name eventRequestName = envelope.createName(requestName, PREFIX, NAMESPACE_URI);
        return request.getSOAPBody().addBodyElement(eventRequestName);
	}

    public String getUserId() {
		return userId;
	}
    
	public void login(String userId, char[] password) throws DaoException {
		SOAPConnection connection = null;
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        Name eventRequestName = envelope.createName("loginRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement loginRequestElement = request.getSOAPBody()
	                .addBodyElement(eventRequestName);
	        addElement(request, loginRequestElement, "userid",   userId);
	        addElement(request, loginRequestElement, "password", String.valueOf(password));
	        addElement(request, loginRequestElement, "role",     "any");
	
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("loginResponse", PREFIX, NAMESPACE_URI);
	        
	        if(response.getSOAPBody().getChildElements(quoteResponseName) != null){
		        SOAPBodyElement responseElement = (SOAPBodyElement) response
		        .getSOAPBody().getChildElements(quoteResponseName).next();
				NodeList nl = responseElement.getElementsByTagName("sessionid");
	
				if(nl != null && nl.getLength() > 0){
					Element element = (Element)nl.item(0);
					sessionId = element.getTextContent();
					this.userId = userId;
			        startEventPolling();
				}
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
	}
	
	public boolean isLogin(){
		if(sessionId != null && sessionId.matches("\\d+")){
			return true;
		}
		return false;
	}
	
	private void checkSOAPFault(SOAPMessage response) throws DaoException, SOAPException {
        if (response.getSOAPBody().hasFault()) {
            SOAPFault fault = response.getSOAPBody().getFault();
            StringBuffer sb = new StringBuffer();
            sb.append("Received SOAP Fault").append("\n");
            sb.append("SOAP Fault Code :" + fault.getFaultCode()).append("\n");
            sb.append("SOAP Fault String :" + fault.getFaultString()).append("\n");
            sb.append("SOAP Fault Text :"+response.getSOAPBody().getTextContent());
            throw new DaoException(sb.toString());
        }
	}
	
	public void logout() throws DaoException {
		SOAPConnection connection = null;
		try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        envelope.getHeader().addChildElement("sessionId", PREFIX, NAMESPACE_URI).setValue(sessionId);
	        Name eventRequestName = envelope.createName("logoutRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement quoteRequestElement = getRequestBodyElement(request, "logoutRequest");
	        addElement(request, quoteRequestElement, "sessionid",   sessionId);
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("logoutResponse", PREFIX, NAMESPACE_URI);
	        
	        if(response.getSOAPBody().getChildElements(quoteResponseName) != null){
		        SOAPBodyElement responseElement = (SOAPBodyElement) response
		        .getSOAPBody().getChildElements(quoteResponseName).next();
				NodeList nl = responseElement.getElementsByTagName("sessionId");
	
				if(nl != null && nl.getLength() > 0){
					Element element = (Element)nl.item(0);
					String status = getStringValue(element, "status");
			        startEventPolling();
				}
	        }
		}catch(SOAPException e){
			throw new DaoException(e);
		}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
	}
	
	private void checkEvent() throws DaoException {
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SOAPConnection connection = null;
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        envelope.getHeader().addChildElement("sessionId", PREFIX, NAMESPACE_URI).setValue(sessionId);
	        
	        Name eventRequestName = envelope.createName("eventRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement eventRequestElement = request.getSOAPBody()
	                .addBodyElement(eventRequestName);
	        eventRequestElement.setValue(sessionId);
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("eventResponse", PREFIX, NAMESPACE_URI);
	        
	        if(response.getSOAPBody().getChildElements(quoteResponseName) != null){
		        SOAPBodyElement responseElement = (SOAPBodyElement) response
		        .getSOAPBody().getChildElements(quoteResponseName).next();
				NodeList nl = responseElement.getElementsByTagName("event");
				
				for(int i = 0; nl != null && i < nl.getLength(); i++){
					if(nl.item(i) == null && !(nl.item(i) instanceof Element)){
						continue;
					}
		
					Element element = (Element)nl.item(i);
					try{
						System.out.println("event:"
								+ "objectId["+getLongValue(element, "objectId")+"]"
								+ "objectType["+getIntValue(element, "objectType")+"]"
								+ "localId["+getStringValue(element, "localId")+"]"
								+ "description["+getStringValue(element, "description")+"]"
								+ "date["+df.parse(getStringValue(element, "date"))+"]"
						);
						for(UpdateListener l:quoteListeners){
							l.updated(new UpdateEvent(
									this,
									getLongValue(element, "objectId"),
									getIntValue(element, "objectType"),
									getStringValue(element, "localId"),
									getStringValue(element, "description"),
									df.parse(getStringValue(element, "date"))
							));
						}
					}catch(ParseException e){
						e.printStackTrace();
					}
		        }
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
    }
	
	public int getIntValue(Element element, String tag){
		String s = getStringValue(element, tag);
		if(s != null && s.trim().matches("^\\d+$")){
			return Integer.parseInt(s);
		}
		throw new NumberFormatException(s);
	}
	
	public long getLongValue(Element element, String tag){
		String s = getStringValue(element, tag);
		if(s != null && s.trim().matches("^\\d+$")){
			return Long.parseLong(s);
		}
		throw new NumberFormatException(s);
	}
	
	public String getStringValue(Element element, String tag){
		NodeList nl = element.getElementsByTagName(tag);
		if(nl != null && nl.getLength() > 0
				&& nl.item(0) != null){
			return nl.item(0).getTextContent();
		}
		return "";
	}
    
    /* (non-Javadoc)
	 * @see com.tamageta.financial.booking.dao.Dao#getUnderlying(java.lang.String)
	 */
    public Underlying getUnderlying(String ric) throws DaoException {
		System.out.println("getUnderlying:"+ric);
    	Underlying underlying = new Underlying();
		SOAPConnection connection = null;
    	
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
//	        envelope.getHeader().addChildElement("sessionId", PREFIX, NAMESPACE_URI).setValue(sessionId);
	        
	        Name quoteRequestName = envelope.createName("underlyingRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement quoteRequestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);
	        quoteRequestElement.setValue(ric);
	
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("underlyingResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement quoteResponseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
	        Iterator ii = quoteResponseElement.getChildElements();
	        while(ii.hasNext()){
	        	Element element = (Element)ii.next();
	        	if("ric".equals(element.getLocalName())){
	        		underlying.setRic(element.getChildNodes().item(0).getNodeValue());
	        	}else if("quick".equals(element.getLocalName())){
	        		underlying.setQuick(element.getChildNodes().item(0).getNodeValue());
	        	}else if("mic".equals(element.getLocalName())){
	        		underlying.setMic(element.getChildNodes().item(0).getNodeValue());
	        	}else if("name".equals(element.getLocalName())){
	        		underlying.setName(element.getChildNodes().item(0).getNodeValue());
	        	}else if("multiplier".equals(element.getLocalName())){
	        		underlying.setMultiplier(Integer.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("open".equals(element.getLocalName())){
	        		underlying.setPriceOpen(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("close".equals(element.getLocalName())){
	        		underlying.setPriceClose(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("high".equals(element.getLocalName())){
	        		underlying.setPriceHigh(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("low".equals(element.getLocalName())){
	        		underlying.setPriceLow(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("volume".equals(element.getLocalName())){
	        		underlying.setVolume(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("avg_volume".equals(element.getLocalName())){
	        		underlying.setAverageVolume(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("shares_out".equals(element.getLocalName())){
	        		underlying.setSharesOut(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("eps_ttm".equals(element.getLocalName())){
	        		underlying.setEpsTtm(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("div".equals(element.getLocalName())){
	        		underlying.setDividend(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("yield".equals(element.getLocalName())){
	        		underlying.setYield(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("ex_div_date".equals(element.getLocalName())){
	        		try{
	        			underlying.setExDivDate(dateFormat.parse(element.getChildNodes().item(0).getNodeValue()));
	        		}catch(ParseException e){
	        			e.printStackTrace();
	        		}
	        	}else if("earnings".equals(element.getLocalName())){
	        		underlying.setEarnings(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("price_earnings".equals(element.getLocalName())){
	        		underlying.setPriceEarnings(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("relative_pe".equals(element.getLocalName())){
	        		underlying.setRelativePe(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("roe".equals(element.getLocalName())){
	        		underlying.setRoe(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("last_div_rep".equals(element.getLocalName())){
	        		underlying.setLastDivRep(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("rel_div_yield".equals(element.getLocalName())){
	        		underlying.setRelDivYield(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("vol90".equals(element.getLocalName())){
	        		underlying.setVol90(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}else if("beta_vs_tpx".equals(element.getLocalName())){
	        		underlying.setBetaVsTpx(Double.valueOf(element.getChildNodes().item(0).getNodeValue()));
	        	}
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
        return underlying;
    }


	public List<Client> getClients() throws DaoException{
		System.out.println("getAccount:"+userId);
    	List<Client> clients = new Vector<Client>();
		SOAPConnection connection = null;
    	
    	if(!isLogin()){
    		return clients;
    	}
    	
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        
	        Name quoteRequestName = envelope.createName("clientInquiryRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);

	        addElement(request, requestElement, "sessionid", sessionId);
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("clientInquiryResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement responseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
	        
	        for(Iterator ii = responseElement.getChildElements();ii.hasNext();){
	        	Element element = (Element)ii.next();
	        	long id = -1;
	        	String companyId = null;
	        	String name = null;
	        	String status = null;

	        	NodeList fields = element.getChildNodes();
	        	for(int i = 0; i < fields.getLength();i++){
		        	Element field = (Element)fields.item(i);
		        	
		        	if("id".equals(field.getTagName())){
		        		id = Long.parseLong(field.getFirstChild().getNodeValue());
		        	}else if("companyId".equals(field.getTagName())){
		        		companyId = field.getFirstChild().getNodeValue();
		        	}else if("name".equals(field.getTagName())){
		        		name = field.getFirstChild().getNodeValue();
		        	}else if("status".equals(field.getTagName())){
		        		status = field.getFirstChild().getNodeValue();
		        	}
	        	}
	        	if(companyId != null && name != null){
	        		clients.add(new Client(id,companyId,name,status));
	        	}
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
    	return clients;
	}

	public List<Client> getClients(String name) throws DaoException{
		ArrayList<Client> list = new ArrayList<Client>();
		for(Client client : getClients()){
			if(client.getName().matches(name)){
				list.add(client);
			}
		}
		return list;
	}
	
	public Client getClient(String clientId) throws DaoException{
		for(Client client : getClients()){
			if(client.getClientId().equals(clientId)){
				return client;
			}
		}
		return null;
	}
	
	public AllocationAccount getAccount(String account) throws DaoException{
		for(AllocationAccount acct: getAccounts()){
			if(acct.getAccount().equals(account)){
				return acct;
			}
		}
		return null;
	}
	
	public List<AllocationAccount> getAccounts() throws DaoException {
		System.out.println("getAccount:"+userId);
    	List<AllocationAccount> accts = new Vector<AllocationAccount>();
		SOAPConnection connection = null;
    	
    	if(!isLogin()){
    		return accts;
    	}
    	
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        
	        Name quoteRequestName = envelope.createName("counterpartyInquiryRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);

	        addElement(request, requestElement, "sessionid", sessionId);
	        addElement(request, requestElement, "companyId", "C1");
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("counterpartyInquiryResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement responseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
	        
	        for(Iterator ii = responseElement.getChildElements();ii.hasNext();){
	        	Element element = (Element)ii.next();
	        	String acct = null;
	        	String name = null;

	        	NodeList fields = element.getChildNodes();
	        	for(int i = 0; i < fields.getLength();i++){
		        	Element field = (Element)fields.item(i);
		        	
		        	if("name".equals(field.getTagName())){
		        		name = field.getFirstChild().getNodeValue();
		        	}
		        	if("account".equals(field.getTagName())){
		        		acct = field.getFirstChild().getNodeValue();
		        	}
	        	}
	        	if(acct != null && name != null){
	        		accts.add(new AllocationAccount(acct,name));
	        	}
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
    	return accts;
	}

	public void saveAccount(List<AllocationAccount> list) throws DaoException{
		System.out.println("saveAccount:"+userId);
    	List<AllocationAccount> accts = new Vector<AllocationAccount>();
		SOAPConnection connection = null;
    	
    	if(!isLogin()){
    		return;
    	}
    	
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        
	        Name quoteRequestName = envelope.createName("counterpartyUpdateRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);

	        addElement(request, requestElement, "sessionid", sessionId);
	        addElement(request, requestElement, "companyId", "C1");
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("counterpartyUpdateResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement responseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
	        
	        for(Iterator ii = responseElement.getChildElements();ii.hasNext();){
	        	Element element = (Element)ii.next();
	        	String acct = null;
	        	String name = null;

	        	NodeList fields = element.getChildNodes();
	        	for(int i = 0; i < fields.getLength();i++){
		        	Element field = (Element)fields.item(i);
		        	
		        	if("name".equals(field.getTagName())){
		        		name = field.getFirstChild().getNodeValue();
		        	}
		        	if("account".equals(field.getTagName())){
		        		acct = field.getFirstChild().getNodeValue();
		        	}
	        	}
	        	if(acct != null && name != null){
	        		accts.add(new AllocationAccount(acct,name));
	        	}
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
	}

	public List<Allocation> getAllocations(Quote quote) throws DaoException{
		System.out.println("getAllocations:"+quote.getQuoteId());
		ArrayList<Allocation> allocs = new ArrayList<Allocation>();
		SOAPConnection connection = null;
    	
    	if(!isLogin()){
    		return allocs;
    	}
    	
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        
	        Name quoteRequestName = envelope.createName("allocationInquiryRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);

	        addElement(request, requestElement, "sessionid", sessionId);
	        addElement(request, requestElement, "quoteId", quote.getQuoteId());
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("allocationInquiryResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement responseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
	        
	        for(Iterator ii = responseElement.getChildElements();ii.hasNext();){
	        	Element element = (Element)ii.next();
	        	String quoteId = null;
	        	long   legId = -1;
	        	String account = null;
	        	double amount = 0;

	        	/*
    		<element name="quoteId"  type="string" maxOccurs="1" />
    		<element name="legId"  type="long" maxOccurs="1" />
    		<element name="account"  type="string" maxOccurs="1" minOccurs="1" />
    		<element name="name"     type="string" maxOccurs="1" minOccurs="0" />
    		<element name="amount"      type="double" maxOccurs="1" minOccurs="1" />
    		<element name="percentage"  type="double" maxOccurs="1" minOccurs="0" />
	        	 */
	        	NodeList fields = element.getChildNodes();
	        	for(int i = 0; i < fields.getLength();i++){
		        	Element field = (Element)fields.item(i);
		        	
		        	if("quoteId".equals(field.getTagName())){
		        		quoteId = field.getFirstChild().getNodeValue();
		        	}
		        	if("legId".equals(field.getTagName())){
		        		legId = Long.parseLong(field.getFirstChild().getNodeValue());
		        	}
		        	if("account".equals(field.getTagName())){
		        		account = field.getFirstChild().getNodeValue();
		        	}
		        	if("amount".equals(field.getTagName())){
		        		amount = Double.parseDouble(field.getFirstChild().getNodeValue());
		        	}
	        	}
	        	if(quoteId != null && legId > -1){
	        		allocs.add(new Allocation(-1, quote.getStrategyLeg(legId),getAccount(account),amount));
	        	}
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
		
		return allocs;
	}
	
	public void saveAllocations(List<Allocation> allocs) throws DaoException{
		System.out.println("getAccount:"+userId);
		SOAPConnection connection = null;
    	
    	if(!isLogin()){
    		return;
    	}
    	
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        
	        Name quoteRequestName = envelope.createName("allocationUpdateRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);
	        addElement(request, requestElement, "sessionid", sessionId);
	        Name allocName = envelope.createName("alloc", PREFIX, NAMESPACE_URI);
	        for(Allocation alloc : allocs){
	        	SOAPElement allocElement = requestElement.addChildElement(allocName);
	        	addElement(request, allocElement, "quoteId", alloc.getStrategyLeg().getQuote().getQuoteId());
	        	addElement(request, allocElement, "legId", Integer.toString(alloc.getStrategyLeg().getLegId()));
	        	addElement(request, allocElement, "account", alloc.getAccount().getAccount());
	        	addElement(request, allocElement, "name", alloc.getAccount().getName());
	        	addElement(request, allocElement, "amount", Double.toString(alloc.getAmount()));
	        	addElement(request, allocElement, "percentage", Double.toString(alloc.getPercentage()));
	        }
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("allocationUpdateResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement responseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
			NodeList nl = responseElement.getElementsByTagName("status");
			
			if(nl != null && nl.getLength() > 0){
				Element element = (Element)nl.item(0);
				if(!"OK".equals(element.getTextContent())){
					throw new DaoException("Allocation failed! ["+element.getTextContent()+"]");
				}
			}
    	}catch(SOAPException e){
    		e.printStackTrace();
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
	}
	
    /* (non-Javadoc)
	 * @see com.tamageta.financial.booking.dao.Dao#sendQuote(com.tamageta.financial.booking.rfq.data.Quote)
	 */
    private void addElement(SOAPMessage request, SOAPElement parent, String name, String value)throws SOAPException {
        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
        SOAPElement element = parent.addChildElement(envelope.createName(name, PREFIX, NAMESPACE_URI));
        element.setValue(value);
    }
    public void sendQuote(Quote quote, String status)throws DaoException {
		SOAPConnection connection = null;
    	if(!isLogin()){
    		return;
    	}
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
//	        envelope.getHeader().addChildElement("sessionId", PREFIX, NAMESPACE_URI).setValue(sessionId);

//	        Name sessionRequestName = envelope.createName("session", PREFIX, NAMESPACE_URI);
//	        SOAPBodyElement sessionRequestElement = request.getSOAPBody()
//            	.addBodyElement(sessionRequestName);
//	        sessionRequestElement.setValue(sessionId);
	        
	        Name quoteRequestName = envelope.createName("quoteRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement quoteRequestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);

	        addElement(request, quoteRequestElement, "sessionid", sessionId);
	        addElement(request, quoteRequestElement, "id", Long.toString(quote.getId()));
	        addElement(request, quoteRequestElement, "quoteId", quote.getQuoteId());
	        addElement(request, quoteRequestElement, "status", status);
	        addElement(request, quoteRequestElement, "tradeDate", Long.toString(quote.getTradeDate().getTime()));
	        addElement(request, quoteRequestElement, "userId", quote.getUserId());
	        addElement(request, quoteRequestElement, "traderId", (quote.getTrader() != null ? quote.getTrader() : ""));
	        addElement(request, quoteRequestElement, "clientId", (quote.getClientId() != null ? quote.getClientId() : ""));
	        addElement(request, quoteRequestElement, "buyOrSell", quote.getBuyOrSell());
	        addElement(request, quoteRequestElement, "openClose", quote.getOpenClose());
	        addElement(request, quoteRequestElement, "premiumSettlement", quote.getPremiumSettlement());
	        addElement(request, quoteRequestElement, "premiumCcy", quote.getPremiumCcy());
	        addElement(request, quoteRequestElement, "underlyingCode", quote.getUnderlyingCode());
	        addElement(request, quoteRequestElement, "spotPrice", Double.toString(quote.getSpotPrice()));
	        addElement(request, quoteRequestElement, "riskFreeRate", Double.toString(quote.getRiskFreeRate()));
	        addElement(request, quoteRequestElement, "strategy", quote.getStrategy());
	        
	        Name strategiesName = envelope.createName("strategyLegs", PREFIX, NAMESPACE_URI);
	        Name strategyName = envelope.createName("strategyLeg", PREFIX, NAMESPACE_URI);
	        SOAPElement strategiesElement = quoteRequestElement.addChildElement(strategiesName);
	        for(StrategyLeg strategy : quote.getStrategies()){
	        	SOAPElement strategyElement = strategiesElement.addChildElement(strategyName);
	        	addElement(request, strategyElement, "id", Long.toString(strategy.getId()));
	        	addElement(request, strategyElement, "legId", Integer.toString(strategy.getLegId()));
	
	        	addElement(request, strategyElement, "ls", strategy.getLs());
	        	addElement(request, strategyElement, "callOrPut", strategy.getCallOrPut());
	        	addElement(request, strategyElement, "quantity", Double.toString(strategy.getQuantity()));
	        	addElement(request, strategyElement, "strike", Double.toString(strategy.getStrike()));
	        	addElement(request, strategyElement, "expiry", dateFormat.format(strategy.getExpiry()));
	        	addElement(request, strategyElement, "multi", Integer.toString(strategy.getMulti()));
	
	        	addElement(request, strategyElement, "volatility", Double.toString(strategy.getVolatility()));
	            addElement(request, strategyElement, "premium", Double.toString(strategy.getPremium()));
	            addElement(request, strategyElement, "delta", Double.toString(strategy.getDelta()));
	            addElement(request, strategyElement, "gamma", Double.toString(strategy.getGamma()));
	            addElement(request, strategyElement, "vega", Double.toString(strategy.getVega()));
	            addElement(request, strategyElement, "theta", Double.toString(strategy.getTheta()));
	            addElement(request, strategyElement, "rho", Double.toString(strategy.getRho()));
	            addElement(request, strategyElement, "volga", Double.toString(strategy.getVolga()));
	            addElement(request, strategyElement, "vanna", Double.toString(strategy.getVanna()));
	            addElement(request, strategyElement, "charm", Double.toString(strategy.getCharm()));
	            addElement(request, strategyElement, "color", Double.toString(strategy.getColor()));
	            addElement(request, strategyElement, "dualDelta", Double.toString(strategy.getDualDelta()));
	            addElement(request, strategyElement, "dualGamma", Double.toString(strategy.getDualGamma()));
	        }
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("quoteResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement quoteResponseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
	        if(!"OK".equals(quoteResponseElement.getValue())){
	        	throw new SOAPException("ERROR:"+quoteResponseElement.getValue());
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
    }
    
	public List<String> getRicCodes(String key) throws DaoException{
		System.out.println("getRicCodes:"+key);
    	List<String> ids = new Vector<String>();
		SOAPConnection connection = null;
    	
    	if(!isLogin()){
    		return ids;
    	}
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
//	        envelope.getHeader().addChildElement("sessionId", PREFIX, NAMESPACE_URI).setValue(sessionId);

//	        Name sessionRequestName = envelope.createName("session", PREFIX, NAMESPACE_URI);
//	        SOAPBodyElement sessionRequestElement = request.getSOAPBody()
//            	.addBodyElement(sessionRequestName);
//	        sessionRequestElement.setValue(sessionId);
	        
	        Name quoteRequestName = envelope.createName("ricListRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);

	        addElement(request, requestElement, "sessionid", sessionId);
	        addElement(request, requestElement, "key", (key != null ? key : ""));
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("ricListResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement responseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
	        Iterator ii = responseElement.getChildElements();
	        while(ii.hasNext()){
	        	Element element = (Element)ii.next();
	        	ids.add(element.getTextContent());
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
    	return ids;
	}
	
	public List<Ric> getUnderlyingRics(String key) throws DaoException{
		System.out.println("getUnderlyingPrices:"+key);
    	List<Ric> ids = new Vector<Ric>();
		SOAPConnection connection = null;
    	
    	if(!isLogin()){
    		return ids;
    	}
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
//	        envelope.getHeader().addChildElement("sessionId", PREFIX, NAMESPACE_URI).setValue(sessionId);

//	        Name sessionRequestName = envelope.createName("session", PREFIX, NAMESPACE_URI);
//	        SOAPBodyElement sessionRequestElement = request.getSOAPBody()
//            	.addBodyElement(sessionRequestName);
//	        sessionRequestElement.setValue(sessionId);
	        
	        Name quoteRequestName = envelope.createName("ricListRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);

	        addElement(request, requestElement, "sessionid", sessionId);
	        addElement(request, requestElement, "key", (key != null ? key : ""));
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("ricListResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement responseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
	        Iterator ii = responseElement.getChildElements();
	        while(ii.hasNext()){
	        	Element element = (Element)ii.next();
	        	NodeList nlCode = element.getElementsByTagName("code");
	        	NodeList nlName = element.getElementsByTagName("name");
	        	if(nlCode.getLength() < 1){
	        		continue;
	        	}
	        	String rc = nlCode.item(0).getTextContent();
	        	String rn = "";
	        	if(nlName.getLength() > 0){
	            	rn = nlName.item(0).getTextContent();
	        	}
	        	ids.add(new Ric(rc,rn));
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
    	return ids;
	}
	
	public List<Price> getUnderlyingPrices(String key, Date startDate, Date endDate) throws DaoException{
		System.out.println("getUnderlyingPrices:"+key+":"+startDate+":"+endDate);
    	List<Price> ids = new Vector<Price>();
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    	Calendar cal = new GregorianCalendar();
		SOAPConnection connection = null;

    	if(!isLogin()){
    		return ids;
    	}

    	cal.setTime(new Date());
    	cal.set(Calendar.DAY_OF_YEAR, -90);
    	
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
//	        envelope.getHeader().addChildElement("sessionId", PREFIX, NAMESPACE_URI).setValue(sessionId);

//	        Name sessionRequestName = envelope.createName("session", PREFIX, NAMESPACE_URI);
//	        SOAPBodyElement sessionRequestElement = request.getSOAPBody()
//            	.addBodyElement(sessionRequestName);
//	        sessionRequestElement.setValue(sessionId);
	        
	        Name quoteRequestName = envelope.createName("priceRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);

	        addElement(request, requestElement, "sessionid", sessionId);
	        addElement(request, requestElement, "key", (key != null ? key : ""));
	        addElement(request, requestElement, "startDate", 
	        		(startDate != null ? df.format(startDate) 
	        				: df.format(cal.getTime())));
	        addElement(request, requestElement, "endDate", 
	        		(endDate != null ? df.format(endDate) : df.format(new Date())));
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("priceResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement responseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
	        Iterator ii = responseElement.getChildElements();
	        while(ii.hasNext()){
	        	Element element = (Element)ii.next();
	        	Price price = new Price();
	        	NodeList nlCode = element.getElementsByTagName("code");
	        	NodeList nlDate = element.getElementsByTagName("timestamp");
	        	NodeList nlOpen = element.getElementsByTagName("open");
	        	NodeList nlClose = element.getElementsByTagName("close");
	        	NodeList nlHigh = element.getElementsByTagName("high");
	        	NodeList nlLow = element.getElementsByTagName("low");
	        	NodeList nlVolume = element.getElementsByTagName("volume");
	        	NodeList nlDivYield = element.getElementsByTagName("div_yield");
	        	NodeList nlYield = element.getElementsByTagName("yield");
				
	        	price.setRic(nlCode.item(0).getTextContent());
	//        	if(nlName.getLength() > 0){
	//        		price.setName(nlName.item(0).getTextContent());
	//        	}
	        	ids.add(price);
	        }
	    	return ids;
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
	}
	
    /* (non-Javadoc)
	 * @see com.tamageta.financial.booking.dao.Dao#getQuotes()
	 */
    public List<Long> getQuoteIds() throws DaoException{
    	System.out.println("getQuoteIds:");
    	List<Long> ids = new Vector<Long>();
		SOAPConnection connection = null;
    	
    	if(!isLogin()){
    		return ids;
    	}

    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        //envelope.getHeader().addChildElement("sessionId", PREFIX, NAMESPACE_URI).setValue(sessionId);
	        
	        Name quoteRequestName = envelope.createName("tradeIdListRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);

	        addElement(request, requestElement, "sessionid", sessionId);
	        addElement(request, requestElement, "startDate", "2007-10-27");
	        addElement(request, requestElement, "endDate", "2007-10-27");
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("tradeIdListResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement responseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
	        Iterator ii = responseElement.getChildElements();
	        while(ii.hasNext()){
	        	Element element = (Element)ii.next();
	        	ids.add(Long.parseLong(element.getTextContent()));
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
    	return ids;
    }

    /* (non-Javadoc)
	 * @see com.tamageta.financial.booking.dao.Dao#getQuote(long)
	 */
    public Quote getQuote(long id) throws DaoException{
    	System.out.println("getQuote:"+id);
		SOAPConnection connection = null;
    	if(!isLogin()){
    		return null;
    	}

    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
        
	        Name quoteRequestName = envelope.createName("tradeInquiryRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);
	        requestElement.setValue(Long.toString(id));
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("tradeInquiryResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement responseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
	        Iterator ii = responseElement.getChildElements();
	        Quote quote = createNewQuote();
	        while(ii.hasNext()){
	        	Element element = (Element)ii.next();
	        	//System.out.println("el["+element.getLocalName()+"]:"+element.getTextContent());
	        	if("id".equals(element.getLocalName())){
	        		quote.setId(Long.parseLong(element.getTextContent()));
	        	}else if("quoteId".equals(element.getLocalName())){
	        		quote.setQuoteId(element.getTextContent());
	        	}else if("tradeDate".equals(element.getLocalName())){
	        		System.out.println(element.getTextContent());
	        		try{
	        			quote.setTradeDate(dateTimeFormat.parse(element.getTextContent()));
	        		}catch(Exception e){}
	        	}else if("status".equals(element.getLocalName())){
	        		quote.setStatus(element.getTextContent());
	        	}else if("userId".equals(element.getLocalName())){
	        		quote.setUserId(element.getTextContent());
	        	}else if("traderId".equals(element.getLocalName())){
	        		quote.setTrader(element.getTextContent());
	        	}else if("clientId".equals(element.getLocalName())){
	        		quote.setClientId(element.getTextContent());
	        	}else if("clientName".equals(element.getLocalName())){
	        		quote.setClientName(element.getTextContent());
	        	}else if("buyOrSell".equals(element.getLocalName())){
	        		quote.setBuyOrSell(element.getTextContent());
	        	}else if("openClose".equals(element.getLocalName())){
	        		quote.setOpenClose(element.getTextContent());
	        	}else if("premiumSettlement".equals(element.getLocalName())){
	        		quote.setPremiumSettlement(element.getTextContent());
	        	}else if("premiumCcy".equals(element.getLocalName())){
	        		quote.setPremiumCcy(element.getTextContent());
	        	}else if("underlyingCode".equals(element.getLocalName())){
	        		quote.setUnderlyingCode(element.getTextContent());
	        	}else if("spotPrice".equals(element.getLocalName())){
	        		try{
	        			quote.setSpotPrice(Double.parseDouble(element.getTextContent()));
	        		}catch(Exception ex){ex.printStackTrace();}
	        	}else if("riskFreeRate".equals(element.getLocalName())){
	        		try{
	        			quote.setRiskFreeRate(Double.parseDouble(element.getTextContent()));
	        		}catch(Exception ex){ex.printStackTrace();}
	        	}else if("strategy".equals(element.getLocalName())){
	        		quote.setStrategy(element.getTextContent());
	        	}else if("strategyLegs".equals(element.getLocalName())){
	        		NodeList i2 = element.getChildNodes();
	        		for(int j = 0; j < i2.getLength(); j++){
	        			if(!(i2.item(j) instanceof Element)){
	        				continue;
	        			}
	            		NodeList lgl = ((Element)i2.item(j)).getChildNodes();
	            		StrategyLeg strategyLeg = quote.addStrategyLeg();
	        			for(int k = 0; k < lgl.getLength(); k++){
	            			if(!(lgl.item(k) instanceof Element)){
	            				continue;
	            			}
	        				Element el = (Element)lgl.item(k);
	        				//System.out.println("el["+el.getLocalName()+"]:"+el.getTextContent());
	        				if("id".equals(el.getLocalName())){
	        					strategyLeg.setId(Long.parseLong(el.getTextContent()));
	        				}else if("legId".equals(el.getLocalName())){
	        					strategyLeg.setLegId(Integer.parseInt(el.getTextContent()));
	        				}else if("ls".equals(el.getLocalName())){
	        					strategyLeg.setLs(el.getTextContent());
	        				}else if("callOrPut".equals(el.getLocalName())){
	        					strategyLeg.setCallOrPut(el.getTextContent());
	        				}else if("quantity".equals(el.getLocalName())){
	        					strategyLeg.setQuantity(Double.parseDouble(el.getTextContent()));
	        				}else if("strike".equals(el.getLocalName())){
	        					strategyLeg.setStrike(Double.parseDouble(el.getTextContent()));
	        				}else if("multi".equals(el.getLocalName())){
	        					strategyLeg.setMulti(Integer.parseInt(el.getTextContent()));
	        				}else if("expiry".equals(el.getLocalName())){
	        					try{
	        						strategyLeg.setExpiry(dateFormat.parse(el.getTextContent()));
	        					}catch(Exception e){
	        						e.printStackTrace();
	        					}
	        				}else if("volatility".equals(el.getLocalName())){
	        					strategyLeg.setVolatility(Double.parseDouble(el.getTextContent()));
	        				}else if("premium".equals(el.getLocalName())){
	        					strategyLeg.setPremium(Double.parseDouble(el.getTextContent()));
	        				}else if("delta".equals(el.getLocalName())){
	        					strategyLeg.setDelta(Double.parseDouble(el.getTextContent()));
	        				}else if("gamma".equals(el.getLocalName())){
	        					strategyLeg.setGamma(Double.parseDouble(el.getTextContent()));
	        				}else if("vega".equals(el.getLocalName())){
	        					strategyLeg.setVega(Double.parseDouble(el.getTextContent()));
	        				}else if("theta".equals(el.getLocalName())){
	        					strategyLeg.setTheta(Double.parseDouble(el.getTextContent()));
	        				}else if("rho".equals(el.getLocalName())){
	        					strategyLeg.setRho(Double.parseDouble(el.getTextContent()));
	        				}else if("volga".equals(el.getLocalName())){
	        					strategyLeg.setVolga(Double.parseDouble(el.getTextContent()));
	        				}else if("vanna".equals(el.getLocalName())){
	        					strategyLeg.setVanna(Double.parseDouble(el.getTextContent()));
	        				}else if("charm".equals(el.getLocalName())){
	        					strategyLeg.setCharm(Double.parseDouble(el.getTextContent()));
	        				}else if("color".equals(el.getLocalName())){
	        					strategyLeg.setColor(Double.parseDouble(el.getTextContent()));
	        				}else if("dualDelta".equals(el.getLocalName())){
	        					strategyLeg.setDualDelta(Double.parseDouble(el.getTextContent()));
	        				}else if("dualGamma".equals(el.getLocalName())){
	        					strategyLeg.setDualGamma(Double.parseDouble(el.getTextContent()));
	        				}
	        			}
	        		}
	        	}
	        }
	    	return quote;
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
    }
    
    public Quote createQuote(Element quoteElement){
    	if(!isLogin()){
    		return null;
    	}

    	Quote quote = createNewQuote();
        NodeList ii = quoteElement.getChildNodes();
        for(int i = 0; i < ii.getLength(); i++){
        	Element element = (Element)ii.item(i);
        	if("id".equals(element.getLocalName())){
        		quote.setId(Long.parseLong(element.getTextContent()));
        	}else if("quoteId".equals(element.getLocalName())){
        		quote.setQuoteId(element.getTextContent());
        	}else if("tradeDate".equals(element.getLocalName())){
        		try{
        			quote.setTradeDate(dateFormat.parse(element.getTextContent()));
        		}catch(Exception e){}
        	}else if("status".equals(element.getLocalName())){
        		quote.setStatus(element.getTextContent());
        	}else if("userId".equals(element.getLocalName())){
        		quote.setUserId(element.getTextContent());
        	}else if("traderId".equals(element.getLocalName())){
        		quote.setTrader(element.getTextContent());
        	}else if("clientId".equals(element.getLocalName())){
        		quote.setClientId(element.getTextContent());
        	}else if("buyOrSell".equals(element.getLocalName())){
        		quote.setBuyOrSell(element.getTextContent());
        	}else if("openClose".equals(element.getLocalName())){
        		quote.setOpenClose(element.getTextContent());
        	}else if("premiumSettlement".equals(element.getLocalName())){
        		quote.setPremiumSettlement(element.getTextContent());
        	}else if("premiumCcy".equals(element.getLocalName())){
        		quote.setPremiumCcy(element.getTextContent());
        	}else if("underlyingCode".equals(element.getLocalName())){
        		quote.setUnderlyingCode(element.getTextContent());
        	}else if("spotPrice".equals(element.getLocalName())){
        		try{
        			quote.setSpotPrice(Double.parseDouble(element.getTextContent()));
        		}catch(Exception ex){ex.printStackTrace();}
        	}else if("riskFreeRate".equals(element.getLocalName())){
        		try{
        			quote.setRiskFreeRate(Double.parseDouble(element.getTextContent()));
        		}catch(Exception ex){ex.printStackTrace();}
        	}else if("strategy".equals(element.getLocalName())){
        		quote.setStrategy(element.getTextContent());
        	}else if("strategyLegs".equals(element.getLocalName())){
        		NodeList i2 = element.getChildNodes();
        		for(int j = 0; j < i2.getLength(); j++){
        			if(!(i2.item(j) instanceof Element)){
        				continue;
        			}
            		NodeList lgl = ((Element)i2.item(j)).getChildNodes();
            		StrategyLeg strategyLeg = quote.addStrategyLeg();
        			for(int k = 0; k < lgl.getLength(); k++){
            			if(!(lgl.item(k) instanceof Element)){
            				continue;
            			}
        				Element el = (Element)lgl.item(k);
        				//System.out.println("el["+el.getLocalName()+"]:"+el.getTextContent());
        				if("id".equals(el.getLocalName())){
        					strategyLeg.setId(Long.parseLong(el.getTextContent()));
        				}else if("legId".equals(el.getLocalName())){
        					strategyLeg.setLegId(Integer.parseInt(el.getTextContent()));
        				}else if("ls".equals(el.getLocalName())){
        					strategyLeg.setLs(el.getTextContent());
        				}else if("callOrPut".equals(el.getLocalName())){
        					strategyLeg.setCallOrPut(el.getTextContent());
        				}else if("quantity".equals(el.getLocalName())){
        					strategyLeg.setQuantity(Double.parseDouble(el.getTextContent()));
        				}else if("strike".equals(el.getLocalName())){
        					strategyLeg.setStrike(Double.parseDouble(el.getTextContent()));
        				}else if("multi".equals(el.getLocalName())){
        					strategyLeg.setMulti(Integer.parseInt(el.getTextContent()));
        				}else if("expiry".equals(el.getLocalName())){
        					try{
        						strategyLeg.setExpiry(dateFormat.parse(el.getTextContent()));
        					}catch(Exception e){
        						e.printStackTrace();
        					}
        				}else if("volatility".equals(el.getLocalName())){
        					strategyLeg.setVolatility(Double.parseDouble(el.getTextContent()));
        				}else if("premium".equals(el.getLocalName())){
        					strategyLeg.setPremium(Double.parseDouble(el.getTextContent()));
        				}else if("delta".equals(el.getLocalName())){
        					strategyLeg.setDelta(Double.parseDouble(el.getTextContent()));
        				}else if("gamma".equals(el.getLocalName())){
        					strategyLeg.setGamma(Double.parseDouble(el.getTextContent()));
        				}else if("vega".equals(el.getLocalName())){
        					strategyLeg.setVega(Double.parseDouble(el.getTextContent()));
        				}else if("theta".equals(el.getLocalName())){
        					strategyLeg.setTheta(Double.parseDouble(el.getTextContent()));
        				}else if("rho".equals(el.getLocalName())){
        					strategyLeg.setRho(Double.parseDouble(el.getTextContent()));
        				}else if("volga".equals(el.getLocalName())){
        					strategyLeg.setVolga(Double.parseDouble(el.getTextContent()));
        				}else if("vanna".equals(el.getLocalName())){
        					strategyLeg.setVanna(Double.parseDouble(el.getTextContent()));
        				}else if("charm".equals(el.getLocalName())){
        					strategyLeg.setCharm(Double.parseDouble(el.getTextContent()));
        				}else if("color".equals(el.getLocalName())){
        					strategyLeg.setColor(Double.parseDouble(el.getTextContent()));
        				}else if("dualDelta".equals(el.getLocalName())){
        					strategyLeg.setDualDelta(Double.parseDouble(el.getTextContent()));
        				}else if("dualGamma".equals(el.getLocalName())){
        					strategyLeg.setDualGamma(Double.parseDouble(el.getTextContent()));
        				}
        			}
        		}
        	}
        }
        
    	return quote;
    }
    
	public Enumeration<Quote> getQuotes() throws DaoException{
		System.out.println("getQuotes");
		SOAPConnection connection = null;
		if(!isLogin()){
    		return null;
    	}
    	
		try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        envelope.getHeader().addChildElement("sessionId", PREFIX, NAMESPACE_URI).setValue(sessionId);
	        Name quoteRequestName = envelope.createName("tradeListRequest", PREFIX, NAMESPACE_URI);

//	        Name sessionRequestName = envelope.createName("session", PREFIX, NAMESPACE_URI);
//	        SOAPBodyElement sessionRequestElement = request.getSOAPBody()
//            	.addBodyElement(sessionRequestName);
//	        sessionRequestElement.setValue(sessionId);
	        
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(quoteRequestName);

	        addElement(request, requestElement, "sessionid", sessionId);
	        addElement(request, requestElement, "startDate", "2007-10-27");
	        addElement(request, requestElement, "endDate", "2007-10-27");
	        
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("tradeListResponse", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement responseElement = (SOAPBodyElement) response
	        .getSOAPBody().getChildElements(quoteResponseName).next();
	
	        Vector<Quote> quotes = new Vector<Quote>();
	        Iterator ii = responseElement.getChildElements();
	        while(ii.hasNext()){
	        	Element quoteElement = (Element)ii.next();
	        	Quote quote = createQuote(quoteElement);
	        	quotes.add(quote);
	        }
			return quotes.elements();
		}catch(SOAPException e){
			throw new DaoException(e);
		}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
	}

    private SOAPMessage createQuoteRequest() throws SOAPException {
    	SOAPMessage message = messageFactory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        envelope.getHeader().addChildElement("sessionId", PREFIX, NAMESPACE_URI).setValue(sessionId);

        Name sessionRequestName = envelope.createName("session", PREFIX, NAMESPACE_URI);
        SOAPBodyElement sessionRequestElement = message.getSOAPBody()
        	.addBodyElement(sessionRequestName);
        sessionRequestElement.setValue(sessionId);
        
        Name quoteRequestName = envelope.createName("quoteRequest", PREFIX, NAMESPACE_URI);
        SOAPBodyElement quoteRequestElement = message.getSOAPBody()
                .addBodyElement(quoteRequestName);
        
        quoteRequestElement.setValue("7203.T");
        return message;
    }

    public void callWebService() throws SOAPException, IOException {
		SOAPConnection connection = null;
        SOAPMessage request = createQuoteRequest();
        try{
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        if (!response.getSOAPBody().hasFault()) {
	            writeQuoteResponse(response);
	        }
	        else {
	            SOAPFault fault = response.getSOAPBody().getFault();
	            System.err.println("Received SOAP Fault");
	            System.err.println("SOAP Fault Code :" + fault.getFaultCode());
	            System.err.println("SOAP Fault String :" + fault.getFaultString());
	        }
        }finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
    }

    private void writeQuoteResponse(SOAPMessage message) throws SOAPException {
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        envelope.getHeader().addChildElement("sessionId", PREFIX, NAMESPACE_URI).setValue(sessionId);
        Name quoteResponseName = envelope.createName("quoteResponse", PREFIX, NAMESPACE_URI);
        SOAPBodyElement quoteResponseElement = (SOAPBodyElement) message
                .getSOAPBody().getChildElements(quoteResponseName).next();
        String quoteValue = quoteResponseElement.getTextContent();
        //System.out.println("Quote Response [" + quoteValue + "]");
    }
    
	public Quote createNewQuote(){
		Quote quote = new QuoteDao();
		try{
			quote.setRiskFreeRate(getDefaultRiskFreeRate());
		}catch(Exception e){
			e.printStackTrace();
		}
		return quote;
	}
	public void takeOwnership(Quote quote) throws DaoException{
		SOAPConnection connection = null;
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        Name eventRequestName = envelope.createName("takeOwnershipRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(eventRequestName);
	        addElement(request, requestElement, "sessionid", sessionId);
	        addElement(request, requestElement, "id",   Long.toString(quote.getId()));
	        addElement(request, requestElement, "userid",   userId);
	
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("takeOwnershipResponse", PREFIX, NAMESPACE_URI);
	        
	        if(response.getSOAPBody().getChildElements(quoteResponseName) != null){
		        SOAPBodyElement responseElement = (SOAPBodyElement) response
		        .getSOAPBody().getChildElements(quoteResponseName).next();
				NodeList nl = responseElement.getElementsByTagName("result");
	
				if(nl != null && nl.getLength() > 0){
					Element element = (Element)nl.item(0);
					if(!"OK".equals(element.getTextContent())){
						throw new DaoException("Cannot take ownership! ["+element.getTextContent()+"]");
					}
				}
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
	}
	public void releaseOwnership(Quote quote) throws DaoException{
		SOAPConnection connection = null;
    	try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        Name eventRequestName = envelope.createName("releaseOwnershipRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(eventRequestName);
	        addElement(request, requestElement, "sessionid", sessionId);
	        addElement(request, requestElement, "id",   Long.toString(quote.getId()));
	        addElement(request, requestElement, "userid",   userId);
	
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("releaseOwnershipResponse", PREFIX, NAMESPACE_URI);
	        
	        if(response.getSOAPBody().getChildElements(quoteResponseName) != null){
		        SOAPBodyElement responseElement = (SOAPBodyElement) response
		        .getSOAPBody().getChildElements(quoteResponseName).next();
				NodeList nl = responseElement.getElementsByTagName("result");
	
				if(nl != null && nl.getLength() > 0){
					Element element = (Element)nl.item(0);
					if(!"OK".equals(element.getTextContent())){
						throw new DaoException("Cannot take ownership! ["+element.getTextContent()+"]");
					}
				}
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
	}

    public static void main(String[] args) throws Exception {
    	//String url = "http://localhost:8081/edsweb/quote/services";
    	//String url = "http://www.tamageta.com/edsweb/quote/services";
    	//String url = "http://192.168.1.5:8081/edsweb/quote/services";
    	//String url = "http://192.168.1.5:8081/edsweb-ken/quote/services";
    	//String url = "http://192.168.1.2:8080/edsweb-ken/quote/services";
    	String url = "http://192.168.1.2:8080/edsweb-ken/quote/services";
        if (args.length > 0) {
            url = args[0];
        }
        
        SoapDao dao = new SoapDao(url);
        //dao.login("ken", new char[]{'a'});
        dao.sessionId = "494";
        
        List<Client> clients = dao.getClients();
        for(Client c : clients){
        	System.out.println("c:"+c.getName());
        }
        System.exit(0);
        
//        List<Allocation> allocs = new ArrayList<Allocation>();
//	      Quote quote = dao.getQuote(356);
//	      for(StrategyLeg leg : quote.getStrategies()){
//	    	  allocs.add(new Allocation(-1, leg, new AllocationAccount("A1", "N"),200.21));
//	      }
//        dao.saveAllocations(allocs);
        
        
//        List<AllocationAccount> list = dao.getAccount();
//        for(AllocationAccount acct : list){
//        	System.out.println("acct:"+acct);
//        }
        
//        Quote quote = new QuoteDao();
//        quote.setUserId("ken");
//        quote.setClientName("DUMMY");
//        quote.setPremiumSettlement("3D");
//        quote.setPremiumCcy("JPY");
//        quote.setUnderlyingCode("7203.T");
//        quote.setUnderlyingName("TOYOTA");
//        quote.setUnderlyingPrimaryMIC("XTKS");
//        quote.setRiskFreeRate(0.001);
//        
//        quote.setStrategy(Quote.STRATEGY_STRANGLE);
//        List<StrategyLeg> strategies = quote.getStrategies();
//        for(StrategyLeg sl : quote.getStrategies()){
//        	sl.setExpiry(new Date());
//        }
//        
//        SoapDao quoteClient = new SoapDao(url);
//        //quoteClient.sendQuote(quote);
//        
//        long id = -1;
//        for(Long l : quoteClient.getQuoteIds()){
//        	System.out.println("qid:"+l.toString());
//        	id = l.longValue();
//        }
//
//        quote = quoteClient.getQuote(id);
//        
//        List<Ric> rics = quoteClient.getUnderlyingRics("T");
//        for(Ric ric : rics){
//        	System.out.println("ric:"+ric.getRic()+":"+ric.getName());
//        }
//        
//        List<Price> prices = quoteClient.getUnderlyingPrices("7203.T", null, null);
//        for(Price price : prices){
//        	System.out.println("price["+price.getRic()+"]"+price.getPrice_timestamp());
//        }
//        
//        quoteClient.checkEvent();
//        
//        Enumeration<Quote> qq = quoteClient.getQuotes();
//        while(qq.hasMoreElements()){
//        	Quote q = qq.nextElement();
//        	System.out.println("quote:"+q);
//        }
        
//        quoteClient.callWebService();
//        Underlying uu = quoteClient.getUnderlying("7203.T");
//        System.out.println("underlying.ric:"+uu.getRic());
//        System.out.println("underlying.name:"+uu.getName());
//        System.out.println("underlying.open:"+uu.getPriceOpen());
//        System.out.println("underlying.ExDivDate:"+uu.getExDivDate());
//        System.out.println("underlying.vol90:"+uu.getVol90());
    }
	public double getDefaultRiskFreeRate() throws DaoException{
		return 0.005;
	}
	public void changeStatus(Quote quote, String newStatus) throws DaoException{
		SOAPConnection connection = null;
		try{
	        SOAPMessage request = messageFactory.createMessage();
	        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	        Name eventRequestName = envelope.createName("changeStatusRequest", PREFIX, NAMESPACE_URI);
	        SOAPBodyElement requestElement = request.getSOAPBody()
	                .addBodyElement(eventRequestName);
	        addElement(request, requestElement, "sessionid", sessionId);
	        addElement(request, requestElement, "id",   Long.toString(quote.getId()));
	        addElement(request, requestElement, "status",   newStatus);
	        addElement(request, requestElement, "userid",   userId);
	
	        connection = connectionFactory.createConnection();
	        SOAPMessage response = connection.call(request, url);
	        checkSOAPFault(response);
	        
	        envelope = request.getSOAPPart().getEnvelope();
	        Name quoteResponseName = envelope.createName("changeStatusResponse", PREFIX, NAMESPACE_URI);
	        
	        if(response.getSOAPBody().getChildElements(quoteResponseName) != null){
		        SOAPBodyElement responseElement = (SOAPBodyElement) response
		        .getSOAPBody().getChildElements(quoteResponseName).next();
				NodeList nl = responseElement.getElementsByTagName("result");
	
				if(nl != null && nl.getLength() > 0){
					Element element = (Element)nl.item(0);
					if(!"OK".equals(element.getTextContent())){
						throw new DaoException("Cannot change status! ["+element.getTextContent()+"]");
					}
				}
	        }
    	}catch(SOAPException e){
    		throw new DaoException(e);
    	}finally{
    		if(connection != null){
    			try{
    				connection.close();
    			}catch(SOAPException e){;}
    		}
    	}
	}
	public Quote createCloseQuote(Quote quote) throws DaoException{
		if(!Quote.OPEN.equals(quote.getOpenClose())){
			throw new DaoException("Quote must be open!");
		}
		Quote newQuote = copyQuote(quote);
		newQuote.setOpenClose(Quote.CLOSE);
		newQuote.setOriginalQuoteId(quote.getQuoteId());
		newQuote.setBuyOrSell(
				Quote.BUY.equals(quote.getBuyOrSell()) ? Quote.SELL : quote.BUY);
		return newQuote;
	}
	public Quote copyQuote(Quote quote) throws DaoException{
		if(!Quote.OPEN.equals(quote.getOpenClose())){
			throw new DaoException("Quote must be open!");
		}
		Quote newQuote = ((QuoteDao)quote).createCopy();
		newQuote.setTradeDate(new Date());
		newQuote.setStatus(Quote.STATUS_NEW);
		newQuote.setNewQuoteId();
		
		for(StrategyLeg leg : newQuote.getStrategies()){
			leg.initCalc();
		}
		return newQuote;
	}
}
