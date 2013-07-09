package com.wesleykerr.steam.etl;

import java.net.URI;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
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
			
			List<URI> hosts = Arrays.asList(new URI("http://127.0.0.1:8091/pools"));
			CouchbaseClient client = new CouchbaseClient(hosts, "default", "");
			View v = client.getView("steam_views", "playtime");
			
			Query q = new Query().setGroup(true).setReduce(true).setIncludeDocs(false);
			
			ViewResponse response = client.query(v, q);
			for (ViewRow row : response) { 
			
			}
//			ViewResult<Long,GameplayStats, ?> entries = v.queryView(Long.class, GameplayStats.class, null);
//			for (ViewResult<Long,GameplayStats,?>.Rows r : entries.getRows()) { 
//				updateOrAdd(r.getKey(), r.getValue());
//			}
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
			"insert into game_recommender.games (appid, total_playtime, recent_playtime) values (?, ?, ?)";
	private static final String UPDATE = 
			"update game_recommender.games set total_playtime = ?, recent_playtime = ? where appid = ?";
}
