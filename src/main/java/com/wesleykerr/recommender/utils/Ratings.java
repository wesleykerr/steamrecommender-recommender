package com.wesleykerr.recommender.utils;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wesleykerr.recommender.TrainingData;
import com.wesleykerr.steam.domain.game.Game;
import com.wesleykerr.steam.persistence.dao.GamesDAO;

public class Ratings { 
    private static final Logger LOGGER = Logger.getLogger(TrainingData.class);

    public static interface RatingsGenerator { 
        /**
         * Return the user's rating for the given game.
         * If they user technically hasn't rated it (not played yet)
         * then return null.
         * @param gameDetails
         * @return
         */
        public Double getRating(JsonObject gameDetails);
    }        

    public static class OwnedRatings implements RatingsGenerator { 
        @Override
        public Double getRating(JsonObject gameDetails) {
            return 1d;
        }
    }
    
    public static class PlayedRatings implements RatingsGenerator { 
        @Override
        public Double getRating(JsonObject gameDetails) {
            // if you played the game for more than 20 minutes.
            if (gameDetails.has("completePlaytime") && 
                    gameDetails.get("completePlaytime").getAsLong() > 20) 
                return 1d;
            return null;
        }
    }

    public static class InferredRatings implements RatingsGenerator { 
        private GamesDAO gameDAO;
        private Cache<Long,Game> gameCache;

        public InferredRatings(GamesDAO gameDAO) { 
            this.gameDAO = gameDAO;
            this.gameCache = CacheBuilder.newBuilder()
                    .maximumSize(10000)
                    .build();
        }
        
        @Override
        public Double getRating(JsonObject gameDetails) {
            if (!gameDetails.has("completePlaytime"))
                return null;
            
            long playtime = gameDetails.get("completePlaytime").getAsLong();
            // if you have played less than 20 minutes
            // you can't really say whether or not you like the game.
            if (playtime < 20) 
                return null;

            JsonElement details = gameDetails.get("appid");
            if (details == null) {
                LOGGER.info("missing appid");
                return null;
            }
            
            long gameId = details.getAsLong();
            Game game = gameCache.getIfPresent(gameId);
            if (game == null) { 
                try { 
                    game = gameDAO.get(gameId);
                    if (game == null) { 
                        LOGGER.info("Missing game: " + gameId);
                    }
                    gameCache.put(gameId, game);
                } catch (Exception e) { 
                    throw new RuntimeException(e);
                }
            }
            
            double hoursPlayed = Math.min(playtime / 60.0, game.getTotalQ75());
            return hoursPlayed / game.getTotalQ75();
        }
        
    }
}
