package com.wesleykerr.steam.persistence.dao;

public interface CounterDAO {
	
	/**
	 * Return the current value of the counter.
	 * @return
	 */
	int getCounter();
	
	/**
	 * Increment the counter variable
	 * and return the new value.
	 * @return
	 */
	int incrCounter();
	
	/**
	 * Reset the counter to zero.
	 */
	void reset();
}
