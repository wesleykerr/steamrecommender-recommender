package com.wesleykerr.steam.api;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.lightcouch.CouchDbClient;
import org.lightcouch.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private PlayerInfo info;
	
	public UpdatePlayers() throws Exception { 
		GregorianCalendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		millis = today.getTime().getTime();
		
		MySQL mySQL = MySQL.getLocalhost();
		Connection conn = mySQL.getConnection();
		genreMap = Utils.loadGenres(conn);
		mySQL.disconnect();

		CounterDAO counter = new CounterDAOImpl();
		queryDoc = new QueryDocument(counter);
		info = new PlayerInfo(queryDoc);
	}
	
	public void runBatch(String view, int batchSize) throws Exception { 
		Gson gson = new Gson();
		BufferedWriter out = new BufferedWriter(new FileWriter("/Users/wkerr/data/steam/player-updates", true));

		CouchDbClient dbClient = new CouchDbClient();
		View v = dbClient.view(view).limit(batchSize).includeDocs(true);
		List<Player> players = v.query(Player.class);
		for (Player p : players) { 
			LOGGER.debug(p.get_id());
			List<GameStats> list = info.gatherOwnedGames(Long.parseLong(p.get_id()), genreMap);
			if (list.size() == 0) 
				p.setVisible(false);
			p.setGames(list);
			p.setUpdateDateTime(millis);
			dbClient.update(p);
			out.write(gson.toJson(p));
			out.write("\n");
			Thread.currentThread().sleep(1500);
		}
		out.close();
	}
	
	public static void main(String[] args) throws Exception { 
		// "week_old" -- "new_players"
		if (args.length != 1) {
			System.out.println("Usage: UpdatePlayers <view-name>");
			System.exit(0);
		}
		String view = args[0];
		System.setProperty("steam.key", "72A809B286ED454CC53C4D03EF798EE4");

		Random r = new Random();
		for (int i = 0; i < 10; ++i) { 
			UpdatePlayers up = new UpdatePlayers();
			up.runBatch("steam-games/" + view, 100);
			
			Thread.currentThread().sleep(5000);
			LOGGER.debug("finished batch " + i);
			if (i % 10 == 0)
				Thread.currentThread().sleep(1000*r.nextInt(30));
		}
	}
}
