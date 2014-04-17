package com.wesleykerr.steam.persistence.dao;

import java.util.Calendar;

import com.wesleykerr.steam.domain.player.Player;

public interface SteamPlayerSampleDAO {

	/**
	 * Add a new player to the database.  
	 * @param p - the details of the player from steam
	 * @param date - the date associated with this scraping
	 */
	public void add(Player p, Calendar date);
    
    /**
     * We are finished with this DAO and close out anything you have 
     * opened and left open for the life of the application.
     */
    public void close();
}
