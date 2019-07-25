package com.tamageta.financial.trading.gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import quickfix.Acceptor;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.ThreadedSocketInitiator;
import quickfix.UnsupportedMessageType;
import quickfix.fix42.Logon;

public class QuickFixDao implements Application{
    private SocketInitiator initiator = null;
    private String configFileName = null;

	public QuickFixDao(){
		
	}
	
	public void setConfigFileName(String fileName){
		this.configFileName = fileName;
	}
	public void init() throws FileNotFoundException, ConfigError{
		init(new FileInputStream(configFileName));
	}
	public void init(InputStream configInputStream) throws FileNotFoundException, ConfigError{
	    SessionSettings settings = new SessionSettings(configInputStream);
	    MessageStoreFactory storeFactory = new FileStoreFactory(settings);
	    LogFactory logFactory = new FileLogFactory(settings);
	    MessageFactory messageFactory = new DefaultMessageFactory();
	    initiator = new SocketInitiator(this, storeFactory, settings, logFactory, messageFactory);
	    initiator.start();
	}
	public void fromAdmin(Message msg, SessionID sesId) throws FieldNotFound,
		IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		System.out.println("fromAdmin:msg["+sesId+"]:"+msg);
	}

	public void fromApp(Message msg, SessionID sesId) throws FieldNotFound,
		IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		System.out.println("fromApp:msg["+sesId+"]:"+msg);
	}

	public void onCreate(SessionID sesId) {
		System.out.println("onCreate:["+sesId+"]");
	}

	public void onLogon(SessionID sesId) {
		System.out.println("onLogon:["+sesId+"]");
	}

	public void onLogout(SessionID sesId) {
		System.out.println("onLogout:["+sesId+"]");
	}

	public void toAdmin(Message msg, SessionID sesId) {
		System.out.println();
		System.out.println("toAdmin:msg["+sesId+"]:"+msg);
	}

	public void toApp(Message msg, SessionID sesId) throws DoNotSend {
		System.out.println("toApp:msg["+sesId+"]:"+msg);
	}
	
	// Applicaton
	public void logIn(){
		
	}
	
	public void logOff(){
		for(SessionID sessionId : initiator.getSessions()){
			Session.lookupSession(sessionId).logout("user requested");
		}
	}
	
	public void sendOrder(){
		
	}
	
	public void sendQuote(){
		
	}
	
	public static void main(String[] args){
		final QuickFixDao dao = new QuickFixDao();
		dao.setConfigFileName("etc/fixClient.conf");
		try{
			dao.init();
		}catch(Throwable e){
			e.printStackTrace();
		}
		Thread t = new Thread(){
			public void run(){
				int cnt = 0;
				while(true){
					System.out.print(".");
					try {
						sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if((cnt++)%10 < 1){
						System.out.println();
					    System.out.println("isLoggedIn:"+dao.initiator.isLoggedOn());
					    System.out.println("SESSION:"+dao.initiator.getSessions());
					}
				}
			}
		};
		t.start();
	}
}
