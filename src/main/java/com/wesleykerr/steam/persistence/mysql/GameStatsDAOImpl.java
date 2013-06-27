package com.wesleykerr.steam.persistence.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.wesleykerr.steam.model.GameStats;
import com.wesleykerr.steam.persistence.GameStatsDAO;

public class GameStatsDAOImpl implements GameStatsDAO {

	private Connection conn;
	
	private PreparedStatement insert;
	private PreparedStatement update;
	
	public GameStatsDAOImpl(Connection conn) { 
		this.conn = conn;
		
		try { 
			insert = conn.prepareStatement(INSERT);
			update = conn.prepareStatement(UPDATE);
		} catch (SQLException e) { 
			
		}
	}
	
	public void insert(GameStats stats) { 
		try {
			insert.setLong(1, stats.getSteamid());
			insert.setLong(2, stats.getAppid());
			insert.setLong(3, stats.getPlayedTwoWeeks());
			insert.setLong(4, stats.getPlayedLifetime());
			insert.executeUpdate();
		} catch (SQLException e) { 
			throw new RuntimeException(e);
		}
	}
	
	public void update(GameStats stats) {
		// try to update the record and if there isn't
		// one, then we insert one.
		try {
			update.setLong(1, stats.getPlayedTwoWeeks());
			update.setLong(2, stats.getPlayedLifetime());
			update.setLong(3, stats.getSteamid());
			update.setLong(4, stats.getAppid());
			int rowsUpdated = update.executeUpdate();
			if (rowsUpdated == 0) {
				insert(stats);
			}
		} catch (SQLException e) { 
			throw new RuntimeException(e);
		}
		
	}

	public void update(List<GameStats> list) {
		// in the future we may be able to batch this
		// to gain a small speed improvement
		for (GameStats stats : list) { 
			update(stats);
		}
	}

	public static final String INSERT = 
			"insert into game_recommender.ownership (`steamid`, `appid`, `playtime_2weeks`, `playtime_forever`) " +
			"values (?, ?, ?, ?) ";
	
	public static final String UPDATE = 
			"update game_recommender.ownership set playtime_2weeks = ?, playtime_forever = ? " + 
		    "where steamid = ? and appid = ? ";

}
