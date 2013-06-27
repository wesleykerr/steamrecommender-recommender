package com.wesleykerr.steam.persistence;

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
}
