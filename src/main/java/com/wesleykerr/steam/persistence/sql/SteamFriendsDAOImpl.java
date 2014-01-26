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
    private PreparedStatement updatePS;
    private PreparedStatement selectPS;
    
    public SteamFriendsDAOImpl(Connection conn) { 
        this.conn = conn;
    }
    
    @Override
    public boolean add(FriendsList friendsList) { 
        long steamId = Long.parseLong(friendsList.getId());
        if (exists(steamId)) {
            update(friendsList);
            return false;
        } else { 
            try { 
                if (insertPS == null)
                    insertPS = conn.prepareStatement(INSERT);

                insertPS.setLong(1, steamId);
                insertPS.setString(2, GsonUtils.getDefaultGson().toJson(friendsList));
                insertPS.setTimestamp(3, new Timestamp(friendsList.getUpdateDateTime()));
                insertPS.executeUpdate();
                
                return true;
            } catch (SQLException e) { 
                throw new RuntimeException(e);
            }
        }
    }
    
    @Override 
    public void update(FriendsList friendsList) { 
        long steamId = Long.parseLong(friendsList.getId());
        if (exists(steamId)) {
            try { 
                if (updatePS == null)
                    updatePS = conn.prepareStatement(UPDATE);

                updatePS.setString(1, GsonUtils.getDefaultGson().toJson(friendsList));
                updatePS.setTimestamp(2, new Timestamp(friendsList.getUpdateDateTime()));
                updatePS.setLong(3, steamId);
                updatePS.executeUpdate();
            } catch (SQLException e) { 
                throw new RuntimeException(e);
            }
        } else { 
            add(friendsList);
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
                return false;
            }
        } catch (SQLException e) { 
            throw new RuntimeException(e);
        }
    }
    
    private static final String INSERT = 
            "INSERT INTO steam_data.friends "
            + "(steamid, revision, content, last_updated) "
            + "VALUES (?, 1, ?, ?) ";
    
    private static final String UPDATE = 
            "UPDATE steam_data.friends "
            + "SET content = ?, "
            + "revision = revision+1, "
            + "last_updated = ? "
            + "WHERE steamid = ?";
    
    private static final String SELECT = 
            "SELECT steamid from steam_data.friends "
            + "WHERE steamid = ? AND content <> NULL ";
}
