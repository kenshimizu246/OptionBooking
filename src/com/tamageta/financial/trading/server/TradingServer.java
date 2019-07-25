package com.tamageta.financial.trading.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
import quickfix.RuntimeError;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.UnsupportedMessageType;

public class TradingServer extends Thread implements Application{
    private Acceptor acceptor = null;
    private volatile boolean stop = false;
    
	public TradingServer(String fileName) {
	    
	    try{
		    SessionSettings settings = new SessionSettings(new FileInputStream(fileName));
		    MessageStoreFactory storeFactory = new FileStoreFactory(settings);
		    LogFactory logFactory = new FileLogFactory(settings);
		    MessageFactory messageFactory = new DefaultMessageFactory();
		    acceptor = new SocketAcceptor(this, storeFactory, settings, logFactory, messageFactory);
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ConfigError e) {
			e.printStackTrace();
	    }
	}
	
	public void run(){
		try{
			acceptor.start();
			while(!stop){
				try {
					sleep(1000);
					System.out.print(".");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (RuntimeError e) {
			e.printStackTrace();
		} catch (ConfigError e) {
			e.printStackTrace();
		}finally{
			acceptor.stop();
		}
	}
	
	public synchronized void setStop(boolean stop){
		this.stop = stop;
		this.notify();
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
		System.out.println("toAdmin:msg["+sesId+"]:"+msg);
	}

	public void toApp(Message msg, SessionID sesId) throws DoNotSend {
		System.out.println("toApp:msg["+sesId+"]:"+msg);
	}
	
	public static void main(String args[]) {
		if(args.length != 1) return;
		String fileName = args[0];

		TradingServer application = new TradingServer(fileName);
    	application.start();
	}
}
