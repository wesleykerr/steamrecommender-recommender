package com.wesleykerr.steam.api;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.gson.GameStats;
import com.wesleykerr.steam.model.Friend;
import com.wesleykerr.steam.model.Player;

public class PlayerInfo {
	private static final Logger LOGGER = LoggerFactory.getLogger(PlayerInfo.class);
	private static final String HOST = "api.steampowered.com";
	
	private QueryDocument queryDocument;
	
	public PlayerInfo(QueryDocument queryDocument) {
		this.queryDocument = queryDocument;
	}
	
	public Player gatherPlayerInfo(long steamId) { 
		String key = System.getProperty("steam.key");
		
    	URIBuilder builder = new URIBuilder();
    	builder.setScheme("http").setHost(HOST).setPath("/ISteamUser/GetPlayerSummaries/v0002/")
    		.setParameter("key", key)
    		.setParameter("steamids", steamId+"");

    	Player player = new Player();
    	player.setSteamId(steamId);
    	try { 
    		JSONObject obj = queryDocument.requestJSON(builder.build(), 2);
    		// break out if our request failed and
    		// let our handler deal with it.
    		if (obj == null)
    			return null;

    		JSONArray array = (JSONArray) ((JSONObject) obj.get("response")).get("players");
			
			// there should only be a single value.
			JSONObject playerObject = (JSONObject) array.get(0);
			
			player.setProfileUrl((String) playerObject.get("profileurl"));
			player.setAvatarUrl((String) playerObject.get("avatar"));
			
			Long lastLogoff = (Long) playerObject.get("lastlogoff");
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(lastLogoff * 1000);
			
			player.setLastOnline(c);
			
    		LOGGER.debug(playerObject.toString());
        	
    	} catch (URISyntaxException e) {
			LOGGER.error(e.getMessage());
		}
    	return player;
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
	
	public List<Friend> gatherFriends(long steamId) { 
		String key = System.getProperty("steam.key");
		
    	URIBuilder builder = new URIBuilder();
    	builder.setScheme("http").setHost(HOST).setPath("/ISteamUser/GetFriendList/v0001/")
    		.setParameter("key", key)
    		.setParameter("steamid", steamId+"");

    	List<Friend> friendsArray = new ArrayList<Friend>();
    	try {
			JSONObject obj = queryDocument.requestJSON(builder.build(), 2);
			JSONArray array = (JSONArray) ((JSONObject) obj.get("friendslist")).get("friends");
			for (Object object : array) { 
				JSONObject jsonObj = (JSONObject) object;

				Friend friend = new Friend();
				friend.setSteamId1(steamId);
				friend.setSteamId2(Long.parseLong((String) jsonObj.get("steamid")));

				Long friendsSince = (Long) jsonObj.get("friend_since");
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(friendsSince * 1000);
				
				friend.setStartDate(c);
				friendsArray.add(friend);
			}
		} catch (URISyntaxException e) {
			LOGGER.error(e.getMessage());
		}
    	
    	return friendsArray;
	}
	
	public void run() { 
		
//		for (Player player : players) {
//			gatherOwnedGames(player.getSteamId());
//		}
	}
}
