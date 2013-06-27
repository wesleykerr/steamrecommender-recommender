package com.wesleykerr.steam.model;

public class GameStats {

	private long steamid;
	private long appid;
	
	private long playedTwoWeeks;
	private long playedLifetime;
	
	public GameStats() { 
		
	}

	/**
	 * @return the steamid
	 */
	public long getSteamid() {
		return steamid;
	}

	/**
	 * @param steamid the steamid to set
	 */
	public void setSteamid(long steamid) {
		this.steamid = steamid;
	}

	/**
	 * @return the appid
	 */
	public long getAppid() {
		return appid;
	}

	/**
	 * @param appid the appid to set
	 */
	public void setAppid(long appid) {
		this.appid = appid;
	}

	/**
	 * @return the playedTwoWeeks
	 */
	public long getPlayedTwoWeeks() {
		return playedTwoWeeks;
	}

	/**
	 * @param playedTwoWeeks the playedTwoWeeks to set
	 */
	public void setPlayedTwoWeeks(long playedTwoWeeks) {
		this.playedTwoWeeks = playedTwoWeeks;
	}

	/**
	 * @return the playedLifetime
	 */
	public long getPlayedLifetime() {
		return playedLifetime;
	}

	/**
	 * @param playedLifetime the playedLifetime to set
	 */
	public void setPlayedLifetime(long playedLifetime) {
		this.playedLifetime = playedLifetime;
	}
	
	
}
