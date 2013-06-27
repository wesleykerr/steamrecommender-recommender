package com.wesleykerr.steam.model;

public class Group {
	
	private int id;
	private String name;
	
	private int lastIndex;
	private String lastPulled;
	
	public Group() { 
		
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the lastIndex
	 */
	public int getLastIndex() {
		return lastIndex;
	}

	/**
	 * @param lastIndex the lastIndex to set
	 */
	public void setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
	}

	/**
	 * @return the lastPulled
	 */
	public String getLastPulled() {
		return lastPulled;
	}

	/**
	 * @param lastPulled the lastPulled to set
	 */
	public void setLastPulled(String lastPulled) {
		this.lastPulled = lastPulled;
	}
	
	
}
