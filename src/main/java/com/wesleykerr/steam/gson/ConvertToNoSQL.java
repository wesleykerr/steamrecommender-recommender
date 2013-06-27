package com.wesleykerr.steam.gson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.lightcouch.CouchDbClient;
import org.lightcouch.Response;

import com.google.gson.Gson;
import com.wesleykerr.steam.Utils;
import com.wesleykerr.steam.persistence.mysql.MySQL;

public class ConvertToNoSQL {

	private GregorianCalendar defaultCal;
	
	private BufferedWriter out;
	private CouchDbClient dbClient;

	private Connection conn;
	private MySQL mySQL;
	
	private Map<Long,List<String>> genreMap;
	
	public ConvertToNoSQL() throws Exception { 
		String logFile = "/tmp/logs.txt";
		out = new BufferedWriter(new FileWriter(logFile));
		dbClient = new CouchDbClient();

		mySQL = MySQL.getLocalhost();
		conn = mySQL.getConnection();
		
		defaultCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		defaultCal.set(Calendar.YEAR, 2000);
		defaultCal.set(Calendar.MONTH, 1);
		defaultCal.set(Calendar.DAY_OF_MONTH, 1);
		defaultCal.set(Calendar.HOUR_OF_DAY, 12);
		defaultCal.set(Calendar.MINUTE, 0);
		defaultCal.set(Calendar.SECOND, 0);

		genreMap = Utils.loadGenres(conn);
	}
	
	public void moveIds() throws Exception { 
		Gson gson = new Gson();
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(id_query);

		while (rs.next()) { 
			Player p = new Player();
			p.set_id(rs.getString("steamid"));
			p.setUpdateDateTime(defaultCal.getTime().getTime());
			p.setVisible(true);
			dbClient.save(p);

			out.write(gson.toJson(p));
			out.write("\n");
		}	
		rs.close();
		s.close();
	}

	public void movePrivatePlayers() throws Exception { 
		Gson gson = new Gson();
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(private_query);

		while (rs.next()) { 
			String steamid = rs.getString("steamid");
			if (dbClient.contains(steamid))
				continue;
			
			Player p = new Player();
			p.set_id(steamid);
			p.setUpdateDateTime(rs.getTimestamp("last_pulled").getTime());
			p.setVisible(false);
			dbClient.save(p);
			
			out.write(gson.toJson(p));
			out.write("\n");
		}	
		rs.close();
		s.close();
	}

	public void movePlayers() throws Exception {
		Gson gson = new Gson();
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(query);

		Player p = null;
		while (rs.next()) { 

			String id = rs.getString("steamid");
			if (p == null) { 
				p = new Player();
				p.set_id(id);
			}
			
			// if we are done, then create the document and store
			// it in our NoSQL solution.
			if (!p.get_id().equals(rs.getString("steamid"))) { 
//				Player tmp = dbClient.find(Player.class, p.get_id());
//				p.set_rev(tmp.get_rev());
//				Response r = dbClient.update(p);
				Response r = dbClient.save(p);
				System.out.println(r.getId());
				out.write(gson.toJson(p));
				out.write("\n");
				
				// create a new player...
				p = new Player();
				p.set_id(id);
				p.setUpdateDateTime(rs.getTimestamp("last_pulled").getTime());
				p.setVisible(true);
			}

			long appid = rs.getLong("appid");
			GameStats stats = new GameStats();
			
			stats.setAppid(appid);
			stats.setGenres(genreMap.get(appid));
			stats.setRecentPlaytime(rs.getLong("playtime_2weeks"));
			stats.setCompletePlaytime(rs.getLong("playtime_forever"));
			
			p.addGame(stats);
		}
		rs.close();
		s.close();
	}
	
	public void close() throws Exception { 
		out.close();
		mySQL.getConnection().close();
		dbClient.shutdown();
	}
	
	public static void main(String[] args) throws Exception { 
		ConvertToNoSQL convert = new ConvertToNoSQL();
		convert.movePlayers();
		convert.movePrivatePlayers();
		convert.moveIds();
		convert.close();
	}
	
	public static final String query = 
			"SELECT p.steamid " + 
			"     , p.last_pulled " +
	        "     , o.appid " +
			"     , o.playtime_2weeks " + 
	        "     , o.playtime_forever " +
	        "FROM game_recommender.players p " +
	        "JOIN game_recommender.ownership o ON (p.steamid = o.steamid) " +
	        "ORDER BY p.steamid, o.appid ";
	
	public static final String private_query = 
			"SELECT steamid, last_pulled FROM game_recommender.players WHERE last_pulled is not null";
	
	public static final String id_query = 
			"SELECT steamid, last_pulled FROM game_recommender.players WHERE last_pulled is null";
}
