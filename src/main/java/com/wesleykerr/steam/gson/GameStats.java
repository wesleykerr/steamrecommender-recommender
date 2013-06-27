package com.wesleykerr.steam.gson;

import java.util.List;

public class GameStats {
	private long appid;
	private List<String> genres;
	
	private long recentPlaytime;
	private long completePlaytime;
	
	public GameStats() {
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
	 * @return the recentPlaytime
	 */
	public long getRecentPlaytime() {
		return recentPlaytime;
	}

	/**
	 * @param recentPlaytime the recentPlaytime to set
	 */
	public void setRecentPlaytime(long recentPlaytime) {
		this.recentPlaytime = recentPlaytime;
	}

	/**
	 * @return the completePlaytime
	 */
	public long getCompletePlaytime() {
		return completePlaytime;
	}

	/**
	 * @param completePlaytime the completePlaytime to set
	 */
	public void setCompletePlaytime(long completePlaytime) {
		this.completePlaytime = completePlaytime;
	}
	
	/**
	 * @return the genres
	 */
	public List<String> getGenres() {
		return genres;
	}

	/**
	 * @param genres the genres to set
	 */
	public void setGenres(List<String> genres) {
		this.genres = genres;
	}
}
