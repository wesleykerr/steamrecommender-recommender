package com.wesleykerr.steam.persistence.nosql;

import java.util.List;
import java.util.concurrent.ExecutionException;

import net.spy.memcached.PersistTo;
import net.spy.memcached.ReplicateTo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.persistence.dao.SteamPlayerDAO;
import com.wesleykerr.utils.GsonUtils;

public class SteamPlayerDAOImpl implements SteamPlayerDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamPlayerDAOImpl.class);
    private CouchbaseClient client;

    public SteamPlayerDAOImpl(CouchbaseClient client) { 
        this.client = client;
    }

    @Override
    public boolean addSteamId(long steamId) {
        Object o = client.get(String.valueOf(steamId));
        if (o == null) {
            JsonObject obj = new JsonObject();
            obj.addProperty("_id", String.valueOf(steamId));
            obj.addProperty("visible", true);
            obj.addProperty("updateDateTime", 0);
            client.add(String.valueOf(steamId), obj.toString(), PersistTo.MASTER, ReplicateTo.ONE);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean exists(long steamId) {
        Object o = client.get(String.valueOf(steamId));
        return o != null;
    }

    @Override
    public void update(Player p) { 
        try {
            client.set(String.valueOf(p.getSteamId()), GsonUtils.getDefaultGson().toJson(p)).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Player> getSteamIdsWithNoFriends(int limit) { 
        return getPlayersFromView("missing_friends", limit);
    }

    @Override
    public List<Player> getRefreshList(int limit) {
        return getPlayersFromView("week_old_players", limit);
    }

    @Override
    public List<Player> getNewPlayers(int limit) {
        return getPlayersFromView("new_players", limit);
    }
    
    private List<Player> getPlayersFromView(String view, int limit) { 
        View v = client.getView("players", view);
        LOGGER.info("view: " + view);
        Query q = new Query().
                setReduce(false).
                setLimit(limit).
                setIncludeDocs(true);
        
        List<Player> players = Lists.newArrayList();
        ViewResponse response = client.query(v, q);
        for (ViewRow row : response) { 
            Player p = GsonUtils.getDefaultGson().fromJson((String) row.getDocument(), Player.class);
            players.add(p);
        }
        return players;
    }

    @Override
    public void close() {

    }
}
