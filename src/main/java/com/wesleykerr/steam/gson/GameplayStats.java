package com.wesleykerr.steam.gson;

public class GameplayStats {
	private long total;
	private long recent;
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
}
