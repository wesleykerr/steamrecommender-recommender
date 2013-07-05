package com.wesleykerr.steam.etl;

import java.sql.PreparedStatement;

import org.lightcouch.CouchDbClient;
import org.lightcouch.View;
import org.lightcouch.ViewResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.gson.GameplayStats;
import com.wesleykerr.steam.persistence.mysql.MySQL;

public class PushPlaytime {
	private static final Logger LOGGER = LoggerFactory.getLogger(PushPlaytime.class);

	private MySQL sql;
	private PreparedStatement psInsert;
	private PreparedStatement psUpdate;
	
	public void run() throws Exception { 
		try { 
			connect();
			CouchDbClient dbClient = new CouchDbClient();
			View v = dbClient.view("steam-games/playtime")
					.group(true)
					.reduce(true)
					.includeDocs(false);

			ViewResult<Long,GameplayStats, ?> entries = v.queryView(Long.class, GameplayStats.class, null);
			for (ViewResult<Long,GameplayStats,?>.Rows r : entries.getRows()) { 
				updateOrAdd(r.getKey(), r.getValue());
			}
		} finally { 
			close();
		}
	}
	
	private void updateOrAdd(Long appid, GameplayStats stats) throws Exception {
		LOGGER.debug("updating: " + appid);
		psUpdate.setLong(1, stats.getTotal());
		psUpdate.setLong(2, stats.getRecent());
		psUpdate.setLong(3, appid);
		int affected = psUpdate.executeUpdate();
		LOGGER.debug("..." + affected);
		if (affected == 0) { 
			LOGGER.debug("inserting: " + appid);
			psInsert.setLong(1, appid);
			psInsert.setLong(2, stats.getTotal());
			psInsert.setLong(3, stats.getRecent());
			psInsert.executeUpdate();
		}
	}
	
	public void connect() throws Exception { 
		sql = MySQL.getDreamhost();
		
		psInsert = sql.getConnection().prepareStatement(INSERT);
		psUpdate = sql.getConnection().prepareStatement(UPDATE);
	}
	
	public void close() throws Exception {
		psInsert.close();
		psUpdate.close();
		
		sql.disconnect();
	}
	
	public static void main(String[] args) throws Exception { 
		PushPlaytime pp = new PushPlaytime();
		pp.run();
	}
	
	private static final String INSERT = 
			"insert into game_recommender.gameplay_stats (appid, total, recent) values (?, ?, ?)";
	private static final String UPDATE = 
			"update game_recommender.gameplay_stats set total = ?, recent = ? where appid = ?";
}
