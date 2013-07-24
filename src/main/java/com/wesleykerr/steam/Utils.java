package com.wesleykerr.steam;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	/**
	 * Take the given JDBC connection and pull out all of the genres by
	 * appid.
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public static Map<Long,List<String>> loadGenres(Connection conn) throws Exception { 
		Map<Long,List<String>> genreMap = new HashMap<Long,List<String>>();
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(genre_query);
		while (rs.next()) { 
			long app = rs.getLong("game_appid");
			String genre = rs.getString("name");
			List<String> genres = genreMap.get(app);
			if (genres == null) { 
				genres = new ArrayList<String>();
				genreMap.put(app, genres);
			}
			genres.add(genre);
		}
		rs.close();
		s.close();
		return genreMap;
	}
	
	/**
	 * Bolierplate code for sleep
	 * @param millis
	 */
	public static void delay(long millis) { 
		try { 
			Thread.sleep(millis);
		} catch (Exception e) { 
			LOGGER.warn(e.getMessage());
		}
	}
	
	public static final String genre_query = 
			"SELECT game_appid, name " +
			"FROM game_recommender.genre_mappings m " +
			"JOIN game_recommender.genres g " +
			"ON (g.id= m.genre_id)";
}
