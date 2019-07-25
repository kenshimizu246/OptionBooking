package com.tamageta.financial.booking.dao;

import java.util.Date;
import java.util.EventObject;

public class UpdateEvent extends EventObject {
	private final long objectId;
	private final int objectType;
	private final Date date;
	private final String localId;
	private final String description;
	
	public static final int TYPE_QUOTE        = 1;
	public static final int TYPE_STRATEGY_LEG = 2;
	public static final int TYPE_UNDERLYING   = 3;
	
     
	public UpdateEvent(Object object, long objectId, int objectType, String localId, String description, Date date){
		super(object);
		this.objectId   = objectId;
		this.objectType = objectType;
		this.localId = localId;
		this.description = description;
		this.date = date;
	}

	public long getObjectId() {
		return objectId;
	}

	public int getObjectType() {
		return objectType;
	}

	public Date getDate() {
		return date;
	}

	public String getLocalId() {
		return localId;
	}

	public String getDescription() {
		return description;
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("ObjectId[").append(objectId).append("],");
		sb.append("ObjectType[").append(objectType).append("],");
		sb.append("localId[").append(localId).append("],");
		sb.append("Date[").append(date).append("],");
		sb.append("description[").append(description).append("]");
		return sb.toString();
	}
}
