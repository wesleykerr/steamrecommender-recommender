package com.wesleykerr.steam.model;

import java.util.Calendar;

public class Friend {

	private long steamId1;
	private long steamId2;
	
	private Calendar startDate;
	
	public Friend() { 
		
	}

	/**
	 * @return the steamId1
	 */
	public long getSteamId1() {
		return steamId1;
	}

	/**
	 * @param steamId1 the steamId1 to set
	 */
	public void setSteamId1(long steamId1) {
		this.steamId1 = steamId1;
	}

	/**
	 * @return the steamId2
	 */
	public long getSteamId2() {
		return steamId2;
	}

	/**
	 * @param steamId2 the steamId2 to set
	 */
	public void setSteamId2(long steamId2) {
		this.steamId2 = steamId2;
	}

	/**
	 * @return the startDate
	 */
	public Calendar getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}
	
	
}
