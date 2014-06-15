package com.wesleykerr.steam.persistence.dao;

import java.util.List;

import com.wesleykerr.steam.domain.game.Game;
import com.wesleykerr.steam.domain.game.GameplayStats;

public interface GamesDAO {

    /**
     * Retrieve the game associated with the
     * given id
     * @param appid
     * @return
     */
    Game get(long appid) throws Exception;
    
    /**
     * Retrieve all of the appids that we know about.
     * @return
     * @throws Exception
     */
    List<Game> getAll() throws Exception;
    
    /**
     * Set the recommendations for the given game.
     * @param appid
     * @param recomms
     * @throws Exception
     */
    void setRecomms(long appid, String recomms) throws Exception;
    
    
    /**
     * Updates the gameplay stats in the database.
     * @param appid
     * @param stats
     * @throws Exception
     */
    void updateOrAddStats(long appid, GameplayStats stats) throws Exception;

    /**
     * Returns a list of games that need to have their images checked.
     * @param all - get all of the images
     * @return
     * @throws Exception
     */
    List<Game> getGamesForImageUpdate(boolean all) throws Exception;
    
    /**
     * Updates the steam url details for the given game.
     * @param game
     * @throws Exception
     */
    void updateUrlDetails(Game game) throws Exception;
    
    /**
     * Returns a list of games that are owned and played.
     * @return
     * @throws Exception
     */
    List<Game> getGamesOwnedOrPlayed() throws Exception;
}
