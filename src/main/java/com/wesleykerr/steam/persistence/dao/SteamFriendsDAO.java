package com.wesleykerr.steam.persistence.dao;

import com.wesleykerr.steam.domain.player.FriendsList;

public interface SteamFriendsDAO {

    public boolean add(FriendsList friends);
    public void update(FriendsList friendsList);
    
    public boolean exists(long steamId);
}
