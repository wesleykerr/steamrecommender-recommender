package com.wesleykerr.steam.persistence.nosql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.JsonObject;
import com.wesleykerr.steam.persistence.dao.SteamPlayerDAO;

public class SteamPlayerDAOImpl implements SteamPlayerDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamPlayerDAOImpl.class);
    private CouchbaseClient client;

    public SteamPlayerDAOImpl(CouchbaseClient client) { 
        this.client = client;
    }
    
    @Override
    public boolean add(long steamId) { 
        Object o = client.get(String.valueOf(steamId));
        if (o == null) {
            JsonObject obj = new JsonObject();
            obj.addProperty("_id", String.valueOf(steamId));
            obj.addProperty("updateDateTime", 0);
            client.add(String.valueOf(steamId), obj.toString());
            return true;
        }
        return false;
    }
}
