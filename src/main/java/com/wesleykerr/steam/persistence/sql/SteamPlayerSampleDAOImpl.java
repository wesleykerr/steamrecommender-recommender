package com.wesleykerr.steam.persistence.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.persistence.dao.SteamPlayerSampleDAO;
import com.wesleykerr.utils.GsonUtils;

public class SteamPlayerSampleDAOImpl implements SteamPlayerSampleDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamPlayerSampleDAOImpl.class);

    private Connection conn;
    
    private PreparedStatement insertPS;

    public SteamPlayerSampleDAOImpl(Connection conn) { 
        this.conn = conn;
    }

	@Override
	public void add(Player p, Calendar date) {
        try { 
            if (insertPS == null)  
                insertPS = conn.prepareStatement(INSERT);

            insertPS.setLong(1, p.getSteamId());
            insertPS.setInt(2, p.getNumGames());
            insertPS.setBoolean(3, p.isPrivate());
            insertPS.setTimestamp(4, new Timestamp(date.getTime().getTime()));
            insertPS.setString(5, GsonUtils.getDefaultGson().toJson(p));
            insertPS.executeUpdate();

        } catch (Exception e) {
            LOGGER.error("Unable to add " + p.getSteamId());
            LOGGER.error("ERROR ", e);
        }
	}

    @Override
    public void close() { 
        closePreparedStatement(insertPS);
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
            "INSERT INTO steam_data.players_sample"
            + "(steamid, num_games, private, last_updated, content) " + 
            " VALUES (?, ?, ?, ?, ?); ";
}
