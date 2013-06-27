package com.wesleykerr.steam.persistence;

import java.util.List;

import com.wesleykerr.steam.model.Player;

public interface PlayerDAO {

	Player find(long steamId);

	boolean add(long steamId, String method);
	void update(Player player);
	
	/**
	 * Return a list of players that need to be updated.
	 * @return
	 */
	List<Long> getPlayers(int max);
	
}
