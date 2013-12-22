package com.wesleykerr.steam.persistence.nosql;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.wesleykerr.steam.domain.player.FriendsList;
import com.wesleykerr.steam.persistence.dao.SteamFriendsDAO;
import com.wesleykerr.utils.GsonUtils;

public class SteamFriendsDAOImpl implements SteamFriendsDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamFriendsDAOImpl.class);
    private CouchbaseClient client;

    public SteamFriendsDAOImpl(CouchbaseClient client) { 
        this.client = client;
    }
    
    @Override
    public boolean add(FriendsList friendsList) { 
        String value = GsonUtils.getDefaultGson().toJson(friendsList);
        Object o = client.get(friendsList.getId());
        if (o == null) { 
            client.add(friendsList.getId(), value);
            return true;
        } else {
            update(friendsList.getId(), value);
            return false;
        }
    }
    
    @Override 
    public void update(FriendsList friendsList) { 
        String value = GsonUtils.getDefaultGson().toJson(friendsList);
        update(friendsList.getId(), value);
    }
    
    private void update(String key, String value) { 
        try {
            client.set(key, value).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
