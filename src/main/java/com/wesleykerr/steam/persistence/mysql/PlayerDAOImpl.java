package com.wesleykerr.steam.persistence.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.model.Player;
import com.wesleykerr.steam.persistence.PlayerDAO;

public class PlayerDAOImpl implements PlayerDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(PlayerDAOImpl.class);

	private Connection conn;

	private PreparedStatement select;
	private PreparedStatement insert;
	private PreparedStatement update;
	
	private PreparedStatement selectDiscovery;
	private PreparedStatement insertDiscovery;
	
	/**
	 * Instanciate a new PlayerDAO object and give it
	 * the database connection necessary.
	 * @param conn
	 */
	public PlayerDAOImpl(Connection conn) {
		this.conn = conn;
		
		// setup the PreparedStatements for this DAO
		try {
			select = conn.prepareStatement(SELECT);
			insert = conn.prepareStatement(INSERT);
			update = conn.prepareStatement(UPDATE);
			
			selectDiscovery = conn.prepareStatement(SELECT_DISCOVERY);
			insertDiscovery = conn.prepareStatement(INSERT_DISCOVERY);
		} catch (Exception e) { 
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * Finished up with this DAO so close out any open statements 
	 */
	public void finish() { 
		try { 
			select.close();
			insert.close();
			update.close();
			
			selectDiscovery.close();
			insertDiscovery.close();
		} catch (Exception e) { 
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public Player find(long steamId) {
		Player player = null;

		ResultSet rs = null;
		try { 
			select.setLong(1, steamId);
			rs = select.executeQuery();
			if (rs.next()) { 
				player = new Player();
				player.setSteamId(steamId);
				player.setProfileUrl(rs.getString("profileurl"));
				player.setAvatarUrl(rs.getString("avatar"));
				
				if (rs.getTimestamp("last_online") != null) {
					Calendar c1 = Calendar.getInstance();
					c1.setTimeInMillis(rs.getTimestamp("last_online").getTime());
					player.setLastOnline(c1);
				}
				
				if (rs.getTimestamp("last_pulled") != null) { 
					Calendar c2 = Calendar.getInstance();
					c2.setTimeInMillis(rs.getTimestamp("last_pulled").getTime());
					player.setLastPulled(c2);
				}
			}
		} catch (SQLException e) { 
			throw new RuntimeException("select failed for " + steamId + " " + e.getMessage());
		} finally { 
			if (rs != null) { 
				try {
					rs.close();
				} catch (Exception e) { 
					throw new RuntimeException("Failed to close!");
				}
			}
		}
		
		return player;
	}
	
	/**
	 * For now we don't allow the outside code to call insert
	 * @param player
	 */
	private void insertPlayer(long steamId) { 
		try { 
			LOGGER.info("INSERT: " + steamId);
			insert.setLong(1, steamId);
			insert.executeUpdate();
		} catch (SQLException e) { 
			throw new RuntimeException("insert failed for " + steamId + " " + e.getMessage());
		} 
	}
	
	/**
	 * Keep track of how we discovered this steamid.
	 * @param steamId
	 * @param method
	 */
	private void insertMethod(long steamId, String method) { 
		ResultSet rs = null;
		try { 
			selectDiscovery.setLong(1, steamId);
			selectDiscovery.setString(2, method);

			rs = selectDiscovery.executeQuery();
			if (rs.next()) { 
				return;
			}
			
			insertDiscovery.setLong(1, steamId);
			insertDiscovery.setString(2, method);
			insertDiscovery.executeUpdate();
			
		} catch (SQLException e) { 
			throw new RuntimeException("insert failed for " + steamId + " " + e.getMessage());
		} finally { 
			if (rs != null) { 
				try {
					rs.close();
				} catch (Exception e) { 
					throw new RuntimeException("Failed to close!");
				}
			}
		}
	}

	public boolean add(long steamId, String method) {
		Player player = find(steamId);
		boolean added = false;
		if (player == null) {
			insertPlayer(steamId);
			added = true;
		}
		
		insertMethod(steamId, method);
		return added;
	}

	public void update(Player player) {

		try { 
			update.setString(1, player.getProfileUrl());
			update.setString(2, player.getAvatarUrl());
			update.setTimestamp(3, new java.sql.Timestamp(player.getLastOnline().getTimeInMillis()));
			update.setLong(4, player.getSteamId());
			update.executeUpdate();
		} catch (SQLException e) { 
			throw new RuntimeException("update failed for " + player.getSteamId() + " " + e.getMessage());
		}
	}
	
	public List<Long> getPlayers(int max) { 
		Statement s = null;
		ResultSet rs = null;
		List<Long> results = new ArrayList<Long>();
		try { 
			s = conn.createStatement();
			rs = s.executeQuery("select steamid " +
					"from game_recommender.players where last_pulled is null limit " + max);
			
			while (rs.next()) { 
				results.add(rs.getLong(1));
			}
			
		} catch (SQLException e) { 
			throw new RuntimeException(e);
		} finally { 
			if (s != null) { 
				try {
					s.close();
				} catch (Exception e) { 
					LOGGER.error("Error closing statement: " + e.getMessage());
				}
			}
			
			if (rs != null) { 
				try { 
					rs.close();
				} catch (Exception e) { 
					LOGGER.error("Error closing statement: " + e.getMessage());
				}
			}
		}
		return results;
	}
	
	private static final String SELECT = 
			"select steamid, profileurl, avatar, last_online, last_pulled " +
			"from game_recommender.players where steamid = ?"; 
	
	private static final String INSERT = 
			"insert into game_recommender.players (`steamid`) values (?) ";
	
	private static final String UPDATE = 
			"update game_recommender.players " +
			"set profileurl=?, avatar=?, last_online=?, last_pulled=CURRENT_TIMESTAMP " +
		    "where steamid=?";

	private static final String SELECT_DISCOVERY = 
			"select discover_datetime from player_discovery where steamid = ? and method = ?";
		
	private static final String INSERT_DISCOVERY = 
			"insert into player_discovery (`steamid`, `method`, `discover_datetime`) " + 
		    "values (?, ?, CURRENT_TIMESTAMP) ";
		

}



