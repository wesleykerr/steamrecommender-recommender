package com.wesleykerr.steam.domain.player;

import java.util.ArrayList;
import java.util.List;


public class Player {
	
	private String _id;
	private String _rev;
	
	private List<GameStats> games;
	private long updateDateTime;
	
	private boolean visible;
	
	public Player() { 
		
	}

	/**
	 * @return the _id
	 */
	public String get_id() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void set_id(String _id) {
		this._id = _id;
	}


	/**
	 * Add a game for this player.
	 * @param stats
	 */
	public void addGame(GameStats stats) { 
		if (games == null) 
			games = new ArrayList<GameStats>();
		games.add(stats);
	}

	/**
	 * @return the games
	 */
	public List<GameStats> getGames() {
		return games;
	}

	/**
	 * @param games the games to set
	 */
	public void setGames(List<GameStats> games) {
		this.games = games;
	}

	/**
	 * @return the updateDateTime
	 */
	public long getUpdateDateTime() {
		return updateDateTime;
	}

	/**
	 * @param updateDateTime the updateDateTime to set
	 */
	public void setUpdateDateTime(long updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return the _rev
	 */
	public String get_rev() {
		return _rev;
	}

	/**
	 * @param _rev the _rev to set
	 */
	public void set_rev(String _rev) {
		this._rev = _rev;
	}
	
}
