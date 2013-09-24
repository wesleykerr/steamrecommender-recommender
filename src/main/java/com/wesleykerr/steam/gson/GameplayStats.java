package com.wesleykerr.steam.gson;


public class GameplayStats {
	private double total;
	private double recent;
	private long owned;
	private long notPlayed;
	private long playedRecently;

	private double totalSquared;
	private double recentSquared;

	/**
	 * @return the total
	 */
	public double getTotal() {
		return total;
	}
	/**
	 * @param total the total to set
	 */
	public void setTotal(double total) {
		this.total = total;
	}
	/**
	 * @return the recent
	 */
	public double getRecent() {
		return recent;
	}
	/**
	 * @param recent the recent to set
	 */
	public void setRecent(double recent) {
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
	/**
	 * @return the totalSquared
	 */
	public double getTotalSquared() {
		return totalSquared;
	}
	/**
	 * @param totalSquared the totalSquared to set
	 */
	public void setTotalSquared(double totalSquared) {
		this.totalSquared = totalSquared;
	}
	/**
	 * @return the recentSquared
	 */
	public double getRecentSquared() {
		return recentSquared;
	}
	/**
	 * @param recentSquared the recentSquared to set
	 */
	public void setRecentSquared(double recentSquared) {
		this.recentSquared = recentSquared;
	}
	
}
