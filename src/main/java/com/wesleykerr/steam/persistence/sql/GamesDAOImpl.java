package com.wesleykerr.steam.persistence.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.domain.game.Game;
import com.wesleykerr.steam.persistence.dao.GamesDAO;

public class GamesDAOImpl implements GamesDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(GamesDAOImpl.class);

    private Connection conn;
    
    private PreparedStatement getPS;
    private PreparedStatement setRecommsPS;
    
    public GamesDAOImpl(Connection conn) { 
        this.conn = conn;
    }

    /* (non-Javadoc)
     * @see com.wesleykerr.steam.persistence.dao.GamesDAO#get(long)
     */
    @Override
    public Game get(long appid) throws Exception {
        if (getPS == null) { 
            getPS = conn.prepareStatement(GET);
        }
        
        getPS.setLong(1, appid);
        try (ResultSet rs = getPS.executeQuery()) {
            if (rs.next()) { 
                Game game = new Game();
                game.setAppid(appid);
                game.setTitle(rs.getString("title"));
                game.setAppType(rs.getString("app_type"));
                game.setOwned(rs.getInt("owned"));
                game.setNotPlayed(rs.getInt("not_played"));

                game.setTotalPlaytime(rs.getDouble("total_playtime"));
                game.setTotalQ25(rs.getDouble("total_q25"));
                game.setTotalQ75(rs.getDouble("total_q75"));
                game.setTotalMedian(rs.getDouble("total_median"));
                
                game.setRecentPlaytime(rs.getDouble("recent_playtime"));
                game.setRecentQ25(rs.getDouble("recent_q25"));
                game.setRecentQ75(rs.getDouble("recent_q75"));
                game.setRecentMedian(rs.getDouble("recent_median"));
                
                game.setMetacritic(rs.getString("metacritic"));
                game.setSteamURL(rs.getString("steam_url"));
                game.setSteamImgURL(rs.getString("steam_img_url"));
                game.setRecomms(rs.getString("recomms"));
                
                Timestamp updatedTS = rs.getTimestamp("updated_datetime");
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(updatedTS.getTime());
                game.setUpdatedDateTime(c);

                Timestamp lastCheckedTS = rs.getTimestamp("last_checked");
                Calendar c2 = Calendar.getInstance();
                c2.setTimeInMillis(lastCheckedTS.getTime());
                game.setLastChecked(c2);
                return game;
            }
        }
        LOGGER.error("Missing game " + appid);
        return null;
    }
    
    @Override
    public void setRecomms(long appid, String recomms) throws Exception {
        if (setRecommsPS == null) 
            setRecommsPS = conn.prepareStatement(SET_RECOMMS);
        
        setRecommsPS.setString(1, recomms);
        setRecommsPS.setLong(2, appid);
        int updated = setRecommsPS.executeUpdate();
        if (updated == 0)
            LOGGER.error("Unable to update " + appid);
    } 

    private static String GET = 
            "select * from game_recommender.games where appid = ?";
    private static String SET_RECOMMS = 
            "update game_recommender.games set recomms = ? where appid = ?";

}
