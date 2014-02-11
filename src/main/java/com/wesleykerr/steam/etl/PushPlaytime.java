package com.wesleykerr.steam.etl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.wesleykerr.steam.domain.game.GameplayStats;
import com.wesleykerr.steam.persistence.MySQL;

public class PushPlaytime {
	private static final Logger LOGGER = LoggerFactory.getLogger(PushPlaytime.class);

	private MySQL sql;
	private PreparedStatement psUpdate;
	
	private CouchbaseClient client;
	private Map<Long,Map<String,Double>> quantileMap;
	
	public void run() throws Exception { 
		try { 
		    loadQuantileInformation("/data/steam/playtime");
			connect();
			Gson gson = new Gson();

			LOGGER.info("Connected to the client, polling the view");
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
	
	private void loadQuantileInformation(String file) throws Exception { 
	    LOGGER.info("Loading " + file);
        quantileMap = Maps.newHashMap();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            while (in.ready()) {
                String line = in.readLine();
                String[] tokens = line.split("\t");
                if (tokens.length != 5) {
                    LOGGER.error("Unknown number of tokens: " + line);
                    throw new RuntimeException("Unknown number of tokens: " + line);
                }
                
                Long appId = Long.parseLong(tokens[0]);
                Map<String,Double> innerMap = quantileMap.get(appId);
                if (innerMap == null) { 
                    innerMap = Maps.newHashMap();
                    quantileMap.put(appId, innerMap);
                }
                
                innerMap.put(tokens[1] + "-q25", Double.parseDouble(tokens[2]));
                innerMap.put(tokens[1] + "-median", Double.parseDouble(tokens[3]));
                innerMap.put(tokens[1] + "-q75", Double.parseDouble(tokens[4]));
            }
        } finally {
            if (in != null)
                in.close();
        }
        LOGGER.info("Loading " + file + " finished");
	}
	
	private void updateOrAdd(Long appid, GameplayStats stats) throws Exception {
//		LOGGER.info("updating: " + appid);
//		psUpdate.setLong(1, stats.getOwned());
//		psUpdate.setLong(2, stats.getNotPlayed());
//		psUpdate.setDouble(3, stats.getTotal());
//
//        psUpdate.setLong(7, stats.getPlayedRecently());
//        psUpdate.setDouble(8, stats.getRecent());
//
//        psUpdate.setLong(12, appid);
//
//		Map<String,Double> map = quantileMap.get(appid);
//		if (map == null) {
//		    LOGGER.error("MISSING: " + appid + " and assuming zeros");
//		    psUpdate.setDouble(4, 0);
//		    psUpdate.setDouble(5, 0);
//		    psUpdate.setDouble(6, 0);
//            psUpdate.setDouble(9, 0);
//            psUpdate.setDouble(10, 0);
//            psUpdate.setDouble(11, 0);
//		} else { 
//            psUpdate.setDouble(4, map.get("total-q25"));
//            psUpdate.setDouble(5, map.get("total-q75"));
//            psUpdate.setDouble(6, map.get("total-median"));
//
//            Double q25 = map.get("recent-q25");
//            psUpdate.setDouble(9, q25 == null ? 0.0 : q25);
//
//            Double q75 = map.get("recent-q75");
//            psUpdate.setDouble(10, q75 == null ? 0.0 : q75);
//            
//            Double median = map.get("recent-median");
//            psUpdate.setDouble(11, median == null ? 0.0 : median);
//		}
//		int affected = psUpdate.executeUpdate();
//		LOGGER.debug("..." + affected);
//		if (affected == 0) { 
//			LOGGER.error("missing: " + appid);
//		}
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
		// check to see if we are already running...
		File lockFile = new File("/tmp/PushPlaytime.lock");
		if (lockFile.exists()) { 
			LOGGER.info("Process already running [" + lockFile.toString() + "]");
			throw new RuntimeException("Process already running!");
		}
		lockFile.createNewFile();
		lockFile.deleteOnExit();
		
		PushPlaytime pp = new PushPlaytime();
		pp.run();
	}
	
	private static final String UPDATE = 
			"update game_recommender.games set owned = ?, not_played = ?, " +
			"total_playtime = ?, total_q25 = ?, total_q75 = ?, total_median = ?, " + 
			"recent_played = ?, recent_playtime = ?, recent_q25 = ?, recent_q75 = ?, recent_median = ? " +
			"where appid = ?";
}
