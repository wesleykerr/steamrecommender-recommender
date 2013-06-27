package com.wesleykerr.steam.model;

import java.util.Calendar;


public class Player {

	private long steamId;
	
	private String profileUrl;
	private String avatarUrl;
	
	// TODO: this could be a Calendar and it
	// would work just fine.
	private Calendar lastOnline;
	private Calendar lastPulled;
	
	public Player() {

	}

	/**
	 * @return the steamId
	 */
	public long getSteamId() {
		return steamId;
	}

	/**
	 * @param steamId the steamId to set
	 */
	public void setSteamId(long steamId) {
		this.steamId = steamId;
	}

	/**
	 * @return the profileUrl
	 */
	public String getProfileUrl() {
		return profileUrl;
	}

	/**
	 * @param profileUrl the profileUrl to set
	 */
	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	/**
	 * @return the avatarUrl
	 */
	public String getAvatarUrl() {
		return avatarUrl;
	}

	/**
	 * @param avatarUrl the avatarUrl to set
	 */
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	/**
	 * @return the lastOnline
	 */
	public Calendar getLastOnline() {
		return lastOnline;
	}

	/**
	 * @param lastOnline the lastOnline to set
	 */
	public void setLastOnline(Calendar lastOnline) {
		this.lastOnline = lastOnline;
	}

	/**
	 * @return the lastPulled
	 */
	public Calendar getLastPulled() {
		return lastPulled;
	}

	/**
	 * @param lastPulled the lastPulled to set
	 */
	public void setLastPulled(Calendar lastPulled) {
		this.lastPulled = lastPulled;
	}

	
}
