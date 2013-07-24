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
import com.google.gson.Gson;
import com.wesleykerr.steam.gson.GameplayStats;
import com.wesleykerr.steam.persistence.mysql.MySQL;

public class PushPlaytime {
	private static final Logger LOGGER = LoggerFactory.getLogger(PushPlaytime.class);

	private MySQL sql;
	private PreparedStatement psUpdate;
	
	private CouchbaseClient client;
	
	public void run() throws Exception { 
		try { 
			connect();
			Gson gson = new Gson();

			View v = client.getView("steam_views", "playtime");
			Query q = new Query().setGroup(true).setGroupLevel(1).setReduce(true).setIncludeDocs(false);
			ViewResponse response = client.query(v, q);
			for (ViewRow row : response) { 
				GameplayStats stats = gson.fromJson(row.getValue(), GameplayStats.class);
				updateOrAdd(Long.parseLong(row.getKey()), stats);
			}
		} finally { 
			close();
		}
	}
	
	private void updateOrAdd(Long appid, GameplayStats stats) throws Exception {
		LOGGER.info("updating: " + appid);
		long totalHours = stats.getTotal() / 60;
		long totalDivisor = stats.getOwned()-stats.getNotPlayed();
	    double totalMean = totalDivisor == 0 ? 0 : totalHours / ((double) totalDivisor);

	    long recentHours = stats.getRecent() / 60;
	    double recentMean = stats.getPlayedRecently() == 0 ? 0 : recentHours / ((double) stats.getPlayedRecently());
		
		psUpdate.setLong(1, stats.getOwned());
		psUpdate.setLong(2, stats.getNotPlayed());
		psUpdate.setLong(3, totalHours);
		psUpdate.setDouble(4, totalMean);
		psUpdate.setLong(5, stats.getPlayedRecently());
		psUpdate.setLong(6, recentHours);
		psUpdate.setDouble(7, recentMean);
		psUpdate.setLong(8, appid);
		int affected = psUpdate.executeUpdate();
		LOGGER.debug("..." + affected);
		if (affected == 0) { 
			LOGGER.error("missing: " + appid);
		}
	}
	
	public void connect() throws Exception { 
		sql = MySQL.getDreamhost();
		psUpdate = sql.getConnection().prepareStatement(UPDATE);

		List<URI> hosts = Arrays.asList(new URI("http://127.0.0.1:8091/pools"));
		client = new CouchbaseClient(hosts, "default", "");
	}
	
	public void close() throws Exception {
		psUpdate.close();
		sql.disconnect();
		
		if (client != null)
			client.shutdown();
	}
	
	public static void main(String[] args) throws Exception { 
		PushPlaytime pp = new PushPlaytime();
		pp.run();
	}
	
	private static final String UPDATE = 
			"update game_recommender.games set owned = ?, not_played = ?, " +
			"total_playtime = ?, total_mean = ?, recent_played = ?, " +
			"recent_playtime = ?, recent_mean = ? where appid = ?";
}
