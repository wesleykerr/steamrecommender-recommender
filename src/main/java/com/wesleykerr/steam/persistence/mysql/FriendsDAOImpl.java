package com.wesleykerr.steam.persistence.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.model.Friend;
import com.wesleykerr.steam.persistence.FriendsDAO;

public class FriendsDAOImpl implements FriendsDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(FriendsDAOImpl.class);

	private Connection conn;
	
	private PreparedStatement insert;
	
	public FriendsDAOImpl(Connection conn) { 
		this.conn = conn;
		
		try { 
			insert = conn.prepareStatement(INSERT);
		} catch (SQLException e) { 
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Finished up with this DAO so close out any open statements 
	 */
	public void finish() { 
		try { 
			insert.close();
		} catch (Exception e) { 
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public void add(Friend friend) {
		try { 
			insert.setLong(1, friend.getSteamId1());
			insert.setLong(2, friend.getSteamId2());
			insert.setTimestamp(3, new java.sql.Timestamp(friend.getStartDate().getTime().getTime()));
			insert.executeUpdate();
		} catch (SQLException e) { 
			// primary key violation
			if (e.getErrorCode() == 1062) { 
				LOGGER.warn("Friend relationship exists: " + friend.getSteamId1() + " " + friend.getSteamId2());
			} else { 
				throw new RuntimeException(e);
			}
		}
	}

	public void add(List<Friend> friends) {
		for (Friend f : friends) { 
			add(f);
		}
	}

	public static final String INSERT = 
			"insert into game_recommender.friends (`steamid_1`, `steamid_2`, `start_date`) " +
			"values (?, ?, ?) ";
	
}
