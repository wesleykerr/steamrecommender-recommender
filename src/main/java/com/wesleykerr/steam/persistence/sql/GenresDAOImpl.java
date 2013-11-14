package com.wesleykerr.steam.persistence.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wesleykerr.steam.persistence.dao.GenresDAO;

public class GenresDAOImpl implements GenresDAO {

    private Connection conn;
    
    public GenresDAOImpl(Connection conn) { 
        this.conn = conn;
    }

    @Override
    public Map<Long, List<String>> getGenresByAppId() throws Exception {
        Map<Long,List<String>> genreMap = new HashMap<Long,List<String>>();
        try (Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery(ALL_GENRE_QRY)) { 
            while (rs.next()) { 
                long app = rs.getLong("game_appid");
                String genre = rs.getString("name");
                List<String> genres = genreMap.get(app);
                if (genres == null) { 
                    genres = new ArrayList<String>();
                    genreMap.put(app, genres);
                }
                genres.add(genre);
            }
        }
        return genreMap;
    }
    
    public static final String ALL_GENRE_QRY = 
            "SELECT game_appid, name " +
            "FROM game_recommender.genre_mappings m " +
            "JOIN game_recommender.genres g " +
            "ON (g.id= m.genre_id)";
}
