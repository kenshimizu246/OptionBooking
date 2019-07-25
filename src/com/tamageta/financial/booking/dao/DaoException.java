package com.tamageta.financial.booking.dao;

public class DaoException extends Exception {

	public DaoException() {
		super();
	}

	public DaoException(String desc) {
		super(desc);
	}

	public DaoException(Throwable t) {
		super(t);
	}

	public DaoException(String desc, Throwable t) {
		super(desc, t);
	}
}
