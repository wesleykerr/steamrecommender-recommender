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
import com.wesleykerr.steam.persistence.dao.SteamPlayerDAO;
import com.wesleykerr.utils.GsonUtils;

public class SteamPlayerDAOImpl implements SteamPlayerDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamPlayerDAOImpl.class);

    private Connection conn;
    
    private PreparedStatement selectPS;
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
            
            Player p = Player.Builder.create()
                    .withSteamId(steamId)
                    .withRevision(0)
                    .build();

            insertPS.setLong(1, steamId);
            insertPS.setString(2, GsonUtils.getDefaultGson().toJson(p));
            insertPS.executeUpdate();
            return true;
        } catch (Exception e) {
            LOGGER.error("Unable to add " + steamId);
            return false;
        }
    }
    
    @Override
    public boolean exists(long steamId) {
        try { 
            if (selectPS == null)
                selectPS = conn.prepareStatement(SELECT);

            selectPS.setLong(1, steamId);
            try (ResultSet rs = selectPS.executeQuery()) { 
                if (rs.next())
                    return true;
            }
            
        } catch (Exception e) { 
            LOGGER.error("Unable to check for existence", e);
        }
        return false;
    }

    @Override
    public void update(Player p) {
        try { 
            if (updatePS == null)  
                updatePS = conn.prepareStatement(UPDATE);
            
            updatePS.setLong(1, p.getSteamId());
            updatePS.setInt(2, p.getRevision());
            
            if (p.getNumGames() == null)
                updatePS.setNull(3, java.sql.Types.INTEGER);
            else
                updatePS.setInt(3, p.getNumGames());
            updatePS.setBoolean(4, p.isPrivate());
            
            if (p.getLastUpdated() != null) 
                updatePS.setTimestamp(5, new Timestamp(p.getLastUpdated()));
            else
                updatePS.setNull(5, java.sql.Types.TIMESTAMP);
            
            if (p.getLastUpdatedFriends() != null) 
                updatePS.setTimestamp(6, new Timestamp(p.getLastUpdatedFriends()));
            else
                updatePS.setNull(6, java.sql.Types.TIMESTAMP);

            updatePS.setString(7, GsonUtils.getDefaultGson().toJson(p));
            
            int affected = updatePS.executeUpdate();
            if (affected == 0)
                LOGGER.error("Unable to update " + p.getSteamId() + " because not there");

        } catch (Exception e) {
            LOGGER.error("Unable to update " + p.getSteamId(), e);
            throw new RuntimeException(e);
        }
    }

    
    @Override
    public List<Player> getRefreshList(int limit) {
        String query = SELECT_REFRESH + " LIMIT " + limit;
        return getPlayers(query);
    }

    @Override
    public List<Player> getNewPlayers(int limit) {
        String query = SELECT_NEW + " LIMIT " + limit;
        return getPlayers(query);
    }

    @Override
    public List<Player> getSteamIdsWithNoFriends(int limit) { 
        String query = SELECT_FRIENDS + " LIMIT " + limit;
        return getPlayers(query);
    }
    
    private List<Player> getPlayers(String query) { 
        List<Player> players = Lists.newArrayList();
        
        LOGGER.info("Executing query " + query);
        try (Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery(query)) { 
            
            while (rs.next()) { 
                String json = rs.getString("content");
                Player p = GsonUtils.getDefaultGson().fromJson(json, Player.class);
                players.add(p);
            }  
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
            "INSERT INTO steam_data.audit_players (steamid, revision, last_updated, content) " + 
            " VALUES (?, 0, '1990-01-01 00:00:00', ?); ";
    
    public static final String UPDATE = 
            "INSERT INTO steam_data.audit_players "
            + "(steamid, revision, num_games, private, "
            + "last_updated, last_updated_friends, content) "
            + "VALUES (?,?,?,?,?,?,?)";
    
    public static final String SELECT = 
            "SELECT steamid FROM steam_data.players WHERE steamid = ?";

    public static final String SELECT_REFRESH = 
            "SELECT content FROM steam_data.players "
            + "WHERE (private is NULL OR private = 0) "
            + "AND last_updated <= date_sub(CURRENT_TIMESTAMP, INTERVAL 7 DAY) "
            + "AND revision > 0 ";
    
    public static final String SELECT_NEW = 
            "SELECT content FROM steam_data.players "
            + "WHERE revision = 0";
    
    public static final String SELECT_FRIENDS = 
            "SELECT content FROM steam_data.players "
            + "WHERE last_updated_friends is NULL";

}
