package com.wesleykerr.steam.etl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.google.gson.Gson;
import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.Utils;
import com.wesleykerr.steam.gson.GameStats;
import com.wesleykerr.steam.gson.Player;
import com.wesleykerr.steam.persistence.CounterDAO;
import com.wesleykerr.steam.persistence.memory.CounterDAOImpl;
import com.wesleykerr.steam.persistence.mysql.MySQL;

public class UpdatePlayers {
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdatePlayers.class);

	private Map<Long,List<String>> genreMap;
	private long millis;
	
	private QueryDocument queryDoc;
	private SteamAPI info;
	
	private CouchbaseClient client;
	
	public UpdatePlayers() throws Exception { 
		GregorianCalendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		millis = today.getTime().getTime();
		
		// TODO: fix this so that we are getting genres from a single
		// source instead of potentially two conflicting sources.
		MySQL mySQL = MySQL.getDreamhost();
		Connection conn = mySQL.getConnection();
		genreMap = Utils.loadGenres(conn);
		mySQL.disconnect();

		CounterDAO counter = new CounterDAOImpl();
		queryDoc = new QueryDocument(counter);
		info = new SteamAPI(queryDoc);

		List<URI> hosts = Arrays.asList(new URI("http://127.0.0.1:8091/pools"));
		client = new CouchbaseClient(hosts, "default", "");
	}
	
	public void runBatch(String view, int batchSize) throws Exception { 
		Gson gson = new Gson();
		BufferedWriter out = new BufferedWriter(new FileWriter("/data/steam/player-updates", true));

		View v = client.getView("players", view);
		LOGGER.info("view: " + view);
		Query q = new Query();
		q.setIncludeDocs(true);
		q.setLimit(batchSize);
		
		ViewResponse response = client.query(v, q);
		for (ViewRow row : response) { 
			Player p = gson.fromJson((String) row.getDocument(), Player.class);
			LOGGER.info("query plalyer " + p.get_id());
			List<GameStats> list = info.gatherOwnedGames(Long.parseLong(p.get_id()), genreMap);
			if (list.size() == 0) 
				p.setVisible(false);
			p.setGames(list);
			p.setUpdateDateTime(millis);

			String updatedDocument = gson.toJson(p);
			client.set(p.get_id(), updatedDocument).get();
			out.write(updatedDocument);
			out.write("\n");
			Thread.currentThread().sleep(1500);
		}
		out.close();
	}
	
	public void finish() { 
		client.shutdown();
	}
	
	public static void main(String[] args) throws Exception { 
		// "week_old_players" -- "new_players"
		if (args.length != 1) {
			System.out.println("Usage: UpdatePlayers <view-name>");
			System.exit(0);
		}
		String view = args[0];
		
		// check to see if we are already running...
		File lockFile = new File("/tmp/UpdatePlayers." + view + ".lock");
		if (lockFile.exists()) { 
			LOGGER.info("Process already running [" + lockFile.toString() + "]");
			throw new RuntimeException("Process already running!");
		}
		lockFile.createNewFile();
		lockFile.deleteOnExit();

		Properties prop = new Properties();
		// TODO make this a parameter that is passed in.
		InputStream input = new FileInputStream("config/recommender.properties");
		LOGGER.info("Input: " + input);
		prop.load(input);
		System.setProperty("steam.key", prop.getProperty("steamKey"));

		UpdatePlayers up = null;
		try { 
			Random r = new Random();
			up = new UpdatePlayers();
			for (int i = 0; i < 10; ++i) { 
				up.runBatch(view, 100);
				
				Thread.currentThread().sleep(5000);
				LOGGER.debug("finished batch " + i);
				if (i % 10 == 0)
					Thread.currentThread().sleep(1000*r.nextInt(30));
			}
		} finally { 
			if (up != null)
				up.finish();
		}
	}
}
