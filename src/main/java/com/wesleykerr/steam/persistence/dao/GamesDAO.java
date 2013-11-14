package com.wesleykerr.steam.persistence.dao;

import com.wesleykerr.steam.domain.game.Game;

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
}
