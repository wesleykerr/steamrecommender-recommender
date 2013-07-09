package com.wesleykerr.steam.etl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.lightcouch.CouchDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import com.wesleykerr.steam.gson.Player;
import com.wesleykerr.steam.persistence.mysql.MySQL;

public class MigrateToCouchbase {
	private static final Logger LOGGER = LoggerFactory.getLogger(MigrateToCouchbase.class);

	private Gson gson;
	
	private CouchDbClient dbClient;
	private CouchbaseClient client;
	
	private Connection conn;
	private MySQL mySQL;
	
	public MigrateToCouchbase() throws Exception {
		gson = new Gson();
		
		dbClient = new CouchDbClient();

		List<URI> hosts = Arrays.asList(new URI("http://127.0.0.1:8091/pools"));
		client = new CouchbaseClient(hosts, "default", "");
		
		mySQL = MySQL.getLocalhost();
		conn = mySQL.getConnection();
	}
	
	private boolean exists(String key) { 
		Object o = client.get(key);
		return o != null;
	}
	
	private String add(String key) throws Exception { 
		Player p = dbClient.find(Player.class, key);
		// change the revision
		p.set_rev(p.get_rev().substring(0, 1));
		
		String result = gson.toJson(p);
		boolean success = client.set(p.get_id(), result).get();
		if (!success) 
			System.out.println("FAILED for some weird reason");
		return result;
	}
	
	public void run() throws Exception { 
		BufferedWriter keys = new BufferedWriter(new FileWriter("/Users/wkerr/data/steam/keys-07082013"));
		BufferedWriter docs = new BufferedWriter(new FileWriter("/Users/wkerr/data/steam/couchbase-07082013"));
		
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(id_query);
		int count = 0;
		while (rs.next()) {
			++count;
			if (count % 10000 == 0)
				LOGGER.error("...processed " + count);

			String id = rs.getString("steamid");
			keys.write(id + "\n");

			String json = add(id);
			docs.write(json + "\n");
		}
		
		keys.close();
		docs.close();
		
		LOGGER.error("Processed all keys");
	}
	
	public static void main(String[] args) throws Exception { 
		MigrateToCouchbase m = new MigrateToCouchbase();
		m.run();
	}

	public static final String id_query = 
			"SELECT steamid FROM game_recommender.players";
}
