package com.wesleykerr.steam.persistence.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.wesleykerr.steam.domain.game.Game;
import com.wesleykerr.steam.domain.game.GameplayStats;
import com.wesleykerr.steam.persistence.dao.GamesDAO;

public class GamesDAOImpl implements GamesDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(GamesDAOImpl.class);

    private Connection conn;
    
    private PreparedStatement getPS;
    private PreparedStatement setRecommsPS;
    
    private PreparedStatement updatePS;
    private PreparedStatement updateUrlPS;
    
    public GamesDAOImpl(Connection conn) { 
        this.conn = conn;
    }
    
    /**
     * Reuse the code to pull out all of the details
     * for a game.
     * @param rs
     * @return
     * @throws Exception
     */
    private Game toGame(ResultSet rs) throws Exception { 
        Game game = new Game();
        game.setAppid(rs.getLong("appid"));
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
        if (updatedTS != null) { 
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(updatedTS.getTime());
            game.setUpdatedDateTime(c);
        } else { 
            LOGGER.info("MIssing updated_datetime : " + game.getAppid());
        }

        Timestamp lastCheckedTS = rs.getTimestamp("last_checked");
        if (lastCheckedTS != null) {
            Calendar c2 = Calendar.getInstance();
            c2.setTimeInMillis(lastCheckedTS.getTime());
            game.setLastChecked(c2);
        }
        return game;
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
                Game game = toGame(rs);
                return game;
            }
        }
        LOGGER.error("Missing game " + appid);
        return null;
    }
    
    @Override
    public List<Game> getGamesForImageUpdate() throws Exception {
        try (Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(GET_IMAGE_UPDATE)) { 

            List<Game> games = Lists.newArrayList();
            while (rs.next()) { 
                games.add(toGame(rs));
            }
            return games;
        }
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

	@Override
	public void updateOrAddStats(long appid, GameplayStats stats) throws Exception {
		if (updatePS == null) 
			updatePS = conn.prepareStatement(UPDATE);
		
		updatePS.setLong(1, stats.getOwned());
		updatePS.setLong(2, stats.getNotPlayed());
		updatePS.setDouble(3, stats.getTotalPlaytime());
		updatePS.setDouble(4, stats.getQ25Playtime());
		updatePS.setDouble(5, stats.getQ75Playtime());
		updatePS.setDouble(6, stats.getMedianPlaytime());
		updatePS.setLong(7, appid);
		
		int updated = updatePS.executeUpdate();
		if (updated == 0)
			LOGGER.error("Unable to update " + appid);
		
	}
	
    @Override
    public void updateUrlDetails(Game game) throws Exception {
        if (updateUrlPS == null) 
            updateUrlPS = conn.prepareStatement(UPDATE_URLS);
        
        updateUrlPS.setString(1, game.getSteamURL());
        updateUrlPS.setString(2, game.getSteamImgURL());
        updateUrlPS.setLong(3, game.getAppid());
        int updated = updateUrlPS.executeUpdate();
        if (updated == 0)
            LOGGER.error("Unable to update " + game.getAppid());
    }


    private static String GET = 
            "select * from game_recommender.games where appid = ?";
    private static String SET_RECOMMS = 
            "update game_recommender.games set recomms = ? where appid = ?";

    private static final String UPDATE = 
			"update game_recommender.games set owned = ?, not_played = ?, " +
			"total_playtime = ?, total_q25 = ?, total_q75 = ?, total_median = ?, " + 
			"where appid = ?";

    private static final String GET_IMAGE_UPDATE = 
            "select * from ("
            + "  select mod(floor(appid / 10),  7) + 1 as hash_value, g.* "
            + "  from game_recommender.games g "
            + "  where (last_checked is null or last_checked <= CURRENT_DATE - 7)) a "
            + "where hash_value = DAYOFWEEK(current_date);";
    
    private static final String UPDATE_URLS = 
            "update game_recommender.games "
            + "set steam_img_url = ?, steam_url = ?, last_checked = CURRENT_TIMESTAMP "
            + "where appid = ?";
}
