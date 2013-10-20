package com.wesleykerr.steam.persistence.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.persistence.dao.CounterDAO;

public class CounterDAOImpl implements CounterDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CounterDAOImpl.class);

	private int count;
	
	/**
	 */
	public CounterDAOImpl() {
		this.count = 0;
	}
	
	/**
	 * Finished up with this DAO so close out any open statements 
	 */
	public void finish() { 
		
	}
	
	/**
	 * Set the counter to the initial value
	 * @param value
	 */
	public void setCounter(int value) { 
		this.count = value;
	}

	public int getCounter() {
		return count;
	}

	public int incrCounter() {
		++count;
		return count;
	}
	
	public void reset() { 
	    count = 0;
	}
}
