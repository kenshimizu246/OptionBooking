package com.tamageta.financial.booking.dao;

public class NotLoginException extends DaoException {

	public NotLoginException() {
		super();
	}

	public NotLoginException(String desc) {
		super(desc);
	}

	public NotLoginException(Throwable t) {
		super(t);
	}

	public NotLoginException(String desc, Throwable t) {
		super(desc, t);
	}
}
