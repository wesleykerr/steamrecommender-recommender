package com.wesleykerr.steam.gson;

public class GameplayStats {
	private long total;
	private long recent;
	private long owned;
	private long notPlayed;
	private long playedRecently;

	/**
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}
	/**
	 * @param total the total to set
	 */
	public void setTotal(long total) {
		this.total = total;
	}
	/**
	 * @return the recent
	 */
	public long getRecent() {
		return recent;
	}
	/**
	 * @param recent the recent to set
	 */
	public void setRecent(long recent) {
		this.recent = recent;
	}
	/**
	 * @return the owned
	 */
	public long getOwned() {
		return owned;
	}
	/**
	 * @param owned the owned to set
	 */
	public void setOwned(long owned) {
		this.owned = owned;
	}
	/**
	 * @return the notPlayed
	 */
	public long getNotPlayed() {
		return notPlayed;
	}
	/**
	 * @param notPlayed the notPlayed to set
	 */
	public void setNotPlayed(long notPlayed) {
		this.notPlayed = notPlayed;
	}
	/**
	 * @return the playedRecently
	 */
	public long getPlayedRecently() {
		return playedRecently;
	}
	/**
	 * @param playedRecently the playedRecently to set
	 */
	public void setPlayedRecently(long playedRecently) {
		this.playedRecently = playedRecently;
	}
	
}
