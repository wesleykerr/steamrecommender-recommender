package com.wesleykerr.steam.persistence.dao;

import java.util.Iterator;
import java.util.List;

import com.wesleykerr.steam.domain.player.Player;

public interface SteamPlayerDAO {

    public boolean add(long id);
    public void update(String id, String document);
    
    public List<Player> getSteamIdsWithNoFriends(int limit);
    
    public Iterator<Player> getPlayers(String tableName, int batchSize);
}
