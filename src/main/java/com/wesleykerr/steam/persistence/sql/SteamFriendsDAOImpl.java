package com.wesleykerr.steam.persistence.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.domain.player.FriendsList;
import com.wesleykerr.steam.persistence.dao.SteamFriendsDAO;
import com.wesleykerr.utils.GsonUtils;

public class SteamFriendsDAOImpl implements SteamFriendsDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamFriendsDAOImpl.class);
    private Connection conn;

    private PreparedStatement insertPS;
    private PreparedStatement selectPS;
    
    public SteamFriendsDAOImpl(Connection conn) { 
        this.conn = conn;
    }
    
    @Override
    public boolean add(FriendsList friendsList) { 
        try { 
            if (insertPS == null)
                insertPS = conn.prepareStatement(INSERT);

            insertPS.setLong(1, friendsList.getSteamId());
            insertPS.setLong(2, friendsList.getRevision());
            if (friendsList.getNumFriends() == null)
                insertPS.setNull(3, java.sql.Types.INTEGER);
            else
                insertPS.setInt(3, friendsList.getNumFriends());
            insertPS.setTimestamp(4, new Timestamp(friendsList.getLastUpdated()));
            insertPS.setString(5, GsonUtils.getDefaultGson().toJson(friendsList));
            insertPS.executeUpdate();
            
            return true;
        } catch (SQLException e) { 
            throw new RuntimeException(e);
        }
    }
    
    @Override 
    public void update(FriendsList friendsList) { 
        add(friendsList);
    }
    
    @Override
    public boolean exists(long steamId) { 
        try { 
            if (selectPS == null) 
                selectPS = conn.prepareStatement(SELECT);

            selectPS.setLong(1, steamId);
            try (ResultSet rs = selectPS.executeQuery()) { 
                if (rs.next()) {
                    LOGGER.info(rs.getLong(1) + " found it...");
                    return true;
                }
                return false;
            }
        } catch (SQLException e) { 
            throw new RuntimeException(e);
        }
    }
    
    private static final String INSERT = 
            "INSERT INTO steam_data.audit_friends "
            + "(steamid, revision, num_friends, last_updated, content) "
            + "VALUES (?, ?, ?, ?, ?) ";
    
    private static final String SELECT = 
            "SELECT steamid from steam_data.friends "
            + "WHERE steamid = ?";
}
