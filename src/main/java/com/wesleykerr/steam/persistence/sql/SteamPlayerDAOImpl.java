package com.wesleykerr.steam.persistence.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.domain.player.Player.Builder;
import com.wesleykerr.steam.persistence.dao.SteamPlayerDAO;
import com.wesleykerr.utils.GsonUtils;

public class SteamPlayerDAOImpl implements SteamPlayerDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamPlayerDAOImpl.class);

    private Connection conn;
    
    private PreparedStatement insertPS;
    private PreparedStatement updatePS;
    private PreparedStatement updateFriendsPS;

    public SteamPlayerDAOImpl(Connection conn) { 
        this.conn = conn;
    }

    @Override
    public boolean addSteamId(long steamId) {
        try { 
            if (insertPS == null)  
                insertPS = conn.prepareStatement(INSERT);
            
            insertPS.setLong(1, steamId);
            insertPS.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Unable to add " + steamId, e);
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public void update(long steamId, int revision, boolean isPrivate, long timestamp, String json) {
        internalUpdate(steamId, revision, isPrivate, timestamp, json, false);
    }
    
    @Override
    public void updatedFriends(long steamId, long timestamp, String json) {
        try { 
            if (updateFriendsPS == null)  
                updateFriendsPS = conn.prepareStatement(UPDATE_FRIENDS);
            
            updateFriendsPS.setTimestamp(1, new Timestamp(timestamp));
            updateFriendsPS.setString(2, json);
            updateFriendsPS.setLong(3, steamId);
            updateFriendsPS.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Unable to add " + steamId, e);
            throw new RuntimeException(e);
        }
    }
    
    private void internalUpdate(long steamId, int revision, boolean isPrivate, 
            long timestamp, String json, boolean added) {
        try { 
            if (updatePS == null)  
                updatePS = conn.prepareStatement(UPDATE);
            
            updatePS.setInt(1, revision);
            updatePS.setTimestamp(2, new Timestamp(timestamp));
            updatePS.setString(3, json);
            updatePS.setBoolean(4, isPrivate);
            updatePS.setLong(5, steamId);
            int affected = updatePS.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Unable to add " + steamId, e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<Player> getRefreshList(int limit) {
        List<Player> players = Lists.newArrayList();
        
        String query = SELECT_REFRESH + " LIMIT " + limit;
        try (Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery(query)) {

            String json = rs.getString("content");
            Player p = GsonUtils.getDefaultGson().fromJson(json, Player.class);

            players.add(p);
            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return players;
    }

    @Override
    public List<Player> getNewPlayers(int limit) {
        String query = SELECT_NEW + " LIMIT " + limit;
        return getSteamIds(query);
    }

    @Override
    public List<Player> getSteamIdsWithNoFriends(int limit) { 
        String query = SELECT_FRIENDS + " LIMIT " + limit;
        return getSteamIds(query);
    }
    
    private List<Player> getSteamIds(String query) { 
        List<Player> players = Lists.newArrayList();
        
        try (Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery(query)) { 
            
            long steamId = rs.getLong("steamid");
            Player.Builder builder = Builder.create().withId(String.valueOf(steamId));
            
            players.add(builder.build());
            
        } catch (SQLException e) { 
            throw new RuntimeException(e);
        }
        
        return players;
    }

    @Override
    public void close() { 
        closePreparedStatement(insertPS);
        closePreparedStatement(updatePS);
        closePreparedStatement(updateFriendsPS);
    }
    
    private void closePreparedStatement(PreparedStatement ps) { 
        if (ps != null) { 
            try { 
                ps.close();
            } catch (SQLException e) { 
                LOGGER.error("Error closing PreparedStatement", e);
            }
        }
    }
    
    public static final String INSERT = 
            "INSERT INTO steam_data.players (steamid, revision) " + 
            " VALUES (?, 0); ";
    
    public static final String UPDATE = 
            "INSERT steam_data.players SET revision = ?, "
            + "modify_datetime = ?, content = ?, "
            + "private = ? WHERE steamid = ?;";
    
    public static final String UPDATE_FRIENDS = 
            "UPDATE steam_data.players "
            + "SET last_updated_friends = ?, content = ? "
            + "WHERE steamid = ?";
    
    public static final String SELECT_REFRESH = 
            "SELECT content FROM steam_data.players "
            + "WHERE private = 0 "
            + "AND last_updated <= date_sub(CURRENT_TIMESTAMP, DAY, 7) ";
    
    public static final String SELECT_NEW = 
            "SELECT steamid FROM steam_data.players "
            + "WHERE private = NULL "
            + "AND content = NULL "
            + "AND modify_datetime = NULL";
    
    public static final String SELECT_FRIENDS = 
            "SELECT steamid FROM steam_data.players "
            + "WHERE last_updated_friends = NULL";

}
