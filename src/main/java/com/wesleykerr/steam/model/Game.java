package com.wesleykerr.steam.model;

public class Game {

	private int appId;
	private String name;
	
	public Game() { 
		
	}

	/**
	 * @return the appId
	 */
	public int getAppId() {
		return appId;
	}

	/**
	 * @param appId the appId to set
	 */
	public void setAppId(int appId) {
		this.appId = appId;
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
	
	
}
