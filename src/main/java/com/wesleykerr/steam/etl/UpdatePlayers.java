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
import com.wesleykerr.steam.domain.player.GameStats;
import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.domain.player.Player.Builder;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.persistence.dao.CounterDAO;
import com.wesleykerr.steam.persistence.dao.GenresDAO;
import com.wesleykerr.steam.persistence.memory.CounterDAOImpl;
import com.wesleykerr.steam.persistence.sql.GenresDAOImpl;
import com.wesleykerr.utils.Utils;

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
		
		GenresDAO genresDAO = new GenresDAOImpl(conn);
		genreMap = genresDAO.getGenresByAppId();
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

			List<GameStats> list = info.gatherOwnedGames(Long.parseLong(p.getId()), genreMap);
            Builder builder = Builder.create()
                    .withPlayer(p)
                    .withGames(list)
                    .isVisible(list.size() > 0)
                    .withUpdateDateTime(millis);
            Player updated = builder.build();
			LOGGER.info("query player " + updated.getId());

			String updatedDocument = gson.toJson(updated);
			client.set(updated.getId(), updatedDocument).get();
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
		if (args.length != 3) {
			System.out.println("Usage: UpdatePlayers <view-name>");
			System.exit(0);
		}
		String view = args[0];
		
		// check to see if we are already running...
		File lockFile = new File("/tmp/UpdatePlayers.lock");
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
