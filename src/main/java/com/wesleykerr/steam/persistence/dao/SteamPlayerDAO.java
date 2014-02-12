package com.wesleykerr.steam.persistence.dao;

import java.util.List;

import com.wesleykerr.steam.domain.player.Player;

public interface SteamPlayerDAO {

	/**
	 * Add a new steam id to the database.  Another
	 * process will eventually pull in all of the details
	 * for the steam id.
	 * @param steamId
	 * @return false if the steam id already exists.
	 */
	public boolean addSteamId(long steamId);

	/**
	 * Check for the existence of a steam id.
	 * @param steamId
	 * @return
	 */
	public boolean exists(long steamId);
		
	/**
     * Update the steam id with the information
     * given.
     * @param p the updated player
     */
    public void update(Player p);

	/**
	 * Get a list of players that need to be refreshed.
	 * @param limit - the maximum number of players to return
	 * @return the players that need to be refreshed.
	 */
	public List<Player> getRefreshList(int limit);
	
	/**
	 * Get a list of players that haven't been scraped from
	 * steam yet.  
	 * @param limit - the maximum number of players to return
	 * @return players without game information.
	 */
	public List<Player> getNewPlayers(int limit);

	/**
	 * Get a list of players that do not have friends stored
	 * in the database yet.  We will use them for pulling in
	 * new steam accounts and to build out the social graph that
	 * currently also powers steam.
	 * @param limit
	 * @return
	 */
    public List<Player> getSteamIdsWithNoFriends(int limit);
    
    
    /**
     * We are finished with this DAO and close out anything you have 
     * opened and left open for the life of the application.
     */
    public void close();
}
