package com.tamageta.financial.booking.rfq.data;

import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public abstract class DataUpdateNotifiableImpl implements DataUpdateNotifiable {
	private Vector<DataUpdateHandler> listeners = new Vector<DataUpdateHandler>();
	private ReentrantLock listenersLock = new ReentrantLock();
	
	/* (non-Javadoc)
	 * @see com.tamageta.financial.booking.rfq.data.DataUpdateNotifiable#addDataUpdateHandler(com.tamageta.financial.booking.rfq.data.DataUpdateHandler)
	 */
	@Override
	public void addDataUpdateHandler(DataUpdateHandler handler){
		try{
			listenersLock.lock();
			if(!listeners.contains(handler)){
				listeners.addElement(handler);
			}
		}finally{
			listenersLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.tamageta.financial.booking.rfq.data.DataUpdateNotifiable#removeDataHandler(com.tamageta.financial.booking.rfq.data.DataUpdateHandler)
	 */
	@Override
	public void removeDataHandler(DataUpdateHandler handler){
		try{
			listenersLock.lock();
			if(listeners.contains(handler)){
				listeners.removeElement(handler);
			}
		}finally{
			listenersLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.tamageta.financial.booking.rfq.data.DataUpdateNotifiable#fireDataUpdateHandler(java.lang.Object, java.lang.String)
	 */
	@Override
	public void fireDataUpdateHandler(DataUpdateEvent event){
		for(DataUpdateHandler h: listeners){
			h.update(event);
		}
	}
}
