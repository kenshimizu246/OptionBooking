package com.tamageta.financial.booking.dao;

import com.tamageta.financial.booking.rfq.data.Underlying;

public class RFQDao {
	public DAOResultSet<Underlying> getUnderlyings(){
		return new DAOResultSet<Underlying>(){
			int i = -1;
			public void close() {}
			public boolean hasNext() {
				return (++i <= 10) ;
			}
			public Underlying next() {
				return new Underlying("7203.T","TOYOTA","XTKY",1000);
			}
		};
	}
}
