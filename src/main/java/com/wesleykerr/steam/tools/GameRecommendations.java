package com.wesleykerr.steam.tools;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.wesleykerr.steam.domain.game.Game;
import com.wesleykerr.steam.domain.game.GameRecomm;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.persistence.dao.GamesDAO;
import com.wesleykerr.steam.persistence.dao.ItemItemModelDAO;
import com.wesleykerr.steam.persistence.sql.GamesDAOImpl;
import com.wesleykerr.steam.persistence.sql.ItemItemModelDAOImpl;

/**
 * Compute the top 4/5 recommendations for each game
 * that we can recommend.  Store this alongside the games.
 * 
 * We will not just store the appid, we want the appid, imgurl, and title
 * in JSON so that we can display quickly and easily.
 * @author wkerr
 *
 */
public class GameRecommendations {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameRecommendations.class);
    
    private int numRecomms;
    private Gson gson;
    
    public GameRecommendations(int numRecomms) { 
        this.numRecomms = numRecomms;
        this.gson = new Gson();
    }
    
    public void updateRecomms(GamesDAO gamesDAO, List<Long> gameIds, 
            String column, long appId) throws Exception { 
        
        List<GameRecomm> recomms = Lists.newArrayList();
        String[] tokens = column.split(",");
        for (int i = 0; i < tokens.length; ++i) {
            if (gameIds.get(i) == appId)
                continue;
            recomms.add(GameRecomm.create(gameIds.get(i), Double.parseDouble(tokens[i])));
        }
        
        List<GameRecomm> ordered = GameRecomm.LARGEST_TO_SMALLEST
                .sortedCopy(recomms)
                .subList(0,numRecomms);
        
        for (GameRecomm recomm : ordered) { 
            Game game = gamesDAO.get(recomm.getAppid());
            if (game == null)
                continue;
            
            recomm.setTitle(game.getTitle());
            recomm.setImgUrl(game.getSteamImgURL());
        }
        gamesDAO.setRecomms(appId, gson.toJson(ordered));
    }
    
    public void run(int modelId) throws Exception { 
        MySQL sql = MySQL.getDreamhost();
        GamesDAO gamesDAO = new GamesDAOImpl(sql.getConnection());
        ItemItemModelDAO cfDAO = new ItemItemModelDAOImpl(sql.getConnection());
        
        String[] column = cfDAO.getColumn(modelId, -1).split(",");
        List<Long> gameIds = Lists.newArrayList();
        for (String s : column) {
            gameIds.add(Long.parseLong(s));
        }
        
        for (Long appid : gameIds)  {
            updateRecomms(gamesDAO, gameIds, cfDAO.getColumn(modelId, appid), appid);
            LOGGER.info("Updated " + appid);
        }
        sql.disconnect();
    }
    
    public static void main(String[] args) throws Exception { 
        File lockFile = new File("/tmp/GameRecommendations.lock");
        if (lockFile.exists()) { 
            LOGGER.info("Process already running [" + lockFile.toString() + "]");
            throw new RuntimeException("Process already running!");
        }
        lockFile.createNewFile();
        lockFile.deleteOnExit();
        
        GameRecommendations recomms = new GameRecommendations(4);
        recomms.run(1);
        
    }
}
