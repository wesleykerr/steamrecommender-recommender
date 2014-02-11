package com.wesleykerr.steam.persistence.dao;

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
}
