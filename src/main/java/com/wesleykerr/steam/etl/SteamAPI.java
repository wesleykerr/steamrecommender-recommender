package com.wesleykerr.steam.etl;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.gson.GameStats;

public class SteamAPI {
	private static final Logger LOGGER = LoggerFactory.getLogger(SteamAPI.class);
	private static final String HOST = "api.steampowered.com";
	
	private QueryDocument queryDocument;

	public SteamAPI(QueryDocument queryDocument) {
		this.queryDocument = queryDocument;
	}

	public List<GameStats> gatherOwnedGames(long steamId, Map<Long,List<String>> genreMap) { 
		String key = System.getProperty("steam.key");
		
    	URIBuilder builder = new URIBuilder();
    	builder.setScheme("http").setHost(HOST).setPath("/IPlayerService/GetOwnedGames/v0001/")
    		.setParameter("key", key)
    		.setParameter("steamid", steamId+"")
    		.setParameter("include_played_free_games", "1");

    	List<GameStats> statsArray = new ArrayList<GameStats>();
    	try {
			JSONObject obj = queryDocument.requestJSON(builder.build(), 2);
			JSONObject response = (JSONObject) obj.get("response");
			if (response.isEmpty()) {
				LOGGER.info("Private profile " + steamId);
				return statsArray;
			}
			LOGGER.debug(obj.toString());
			
			JSONArray array = (JSONArray) response.get("games");
			if (array == null)
				return statsArray;
			
			for (Object object : array) { 
				JSONObject jsonObj = (JSONObject) object;
				GameStats stats = new GameStats();
				stats.setAppid((Long) jsonObj.get("appid"));
				stats.setGenres(genreMap.get(stats.getAppid()));
				
				if (jsonObj.containsKey("playtime_forever")) { 
					stats.setCompletePlaytime((Long) jsonObj.get("playtime_forever"));
				}
				
				if (jsonObj.containsKey("playtime_2weeks")) {
					stats.setRecentPlaytime((Long) jsonObj.get("playtime_2weeks"));
				}
				statsArray.add(stats);
			}
		} catch (URISyntaxException e) {
			LOGGER.error(e.getMessage());
		}
    	
    	return statsArray;
	}
}
