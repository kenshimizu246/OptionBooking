package com.tamageta.financial.booking.dao;

public abstract class DAOResultSet<T> {
	public abstract boolean hasNext();
	public abstract T next();
	public abstract void close();
}
