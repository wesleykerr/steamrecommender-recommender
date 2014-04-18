package com.wesleykerr.steam.etl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.domain.player.FriendsList.Relationship;
import com.wesleykerr.steam.domain.player.GameStats;
import com.wesleykerr.utils.GsonUtils;

public class SteamAPI {
	private static final Logger LOGGER = LoggerFactory.getLogger(SteamAPI.class);
	private static final String HOST = "api.steampowered.com";
	
	private QueryDocument queryDocument;

	private String steamKey = null;
	private String userAgent = null;
	
	public SteamAPI(QueryDocument queryDocument) {
		this.queryDocument = queryDocument;
		
		loadSteamProps();
	}
	
	private void loadSteamProps() { 
	    try { 
	        Properties prop = new Properties();
	        // TODO make this a parameter that is passed in.
	        InputStream input = new FileInputStream("config/recommender.properties");

	        prop.load(input);
	        steamKey = prop.getProperty("steamKey");
	        userAgent = prop.getProperty("userAgent");
	    } catch (Exception e) { 
	        throw new RuntimeException(e);
	    }
	}
	
	public List<Relationship> gatherFriends(long steamId) { 
	    Preconditions.checkNotNull(steamKey);
	    Preconditions.checkNotNull(userAgent);
        
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(HOST).setPath("/ISteamUser/GetFriendList/v0001/")
            .setParameter("key", steamKey)
            .setParameter("steamid", String.valueOf(steamId ))
            .setParameter("relationship", "friend");
        
        try { 
            JsonObject obj = queryDocument.requestJSON(builder.build(), userAgent, 2);
            if (!obj.has("friendslist")) {
                LOGGER.info("Private profile " + steamId);
                return null;
            }

            List<Relationship> list = Lists.newArrayList();
            JsonObject friendsObj = obj.get("friendslist").getAsJsonObject();
            JsonArray friendsArray = friendsObj.get("friends").getAsJsonArray();
            for (JsonElement element : friendsArray) { 
                list.add(GsonUtils.getDefaultGson().fromJson(element.toString(), Relationship.class));
            }
            return list;
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
	}

	public List<GameStats> gatherOwnedGames(long steamId) { 
        Preconditions.checkNotNull(steamKey);
        Preconditions.checkNotNull(userAgent);
		
    	URIBuilder builder = new URIBuilder();
    	builder.setScheme("http").setHost(HOST).setPath("/IPlayerService/GetOwnedGames/v0001/")
    		.setParameter("key", steamKey)
    		.setParameter("steamid", steamId+"")
    		.setParameter("include_played_free_games", "1");

    	List<GameStats> statsArray = new ArrayList<GameStats>();
    	try {
			JsonObject obj = queryDocument.requestJSON(builder.build(), userAgent, 2);
			JsonObject response = obj.get("response").getAsJsonObject();
			if (!response.has("games")) {
				LOGGER.debug("Returning null for " + steamId);
				return null;
			}
			LOGGER.debug(obj.toString());
			
			JsonArray array = response.get("games").getAsJsonArray();
			if (array == null)
				return statsArray;
			
			for (JsonElement element : array) { 
			    JsonObject jsonObj = element.getAsJsonObject();
				GameStats stats = new GameStats();
				stats.setAppid(jsonObj.get("appid").getAsLong());
				
				if (jsonObj.has("playtime_forever")) { 
					stats.setCompletePlaytime(jsonObj.get("playtime_forever").getAsLong());
				}
				
				if (jsonObj.has("playtime_2weeks")) {
					stats.setRecentPlaytime(jsonObj.get("playtime_2weeks").getAsLong());
				}
				statsArray.add(stats);
			}
		} catch (URISyntaxException e) {
			LOGGER.error(e.getMessage());
		}
    	
    	return statsArray;
	}
}
