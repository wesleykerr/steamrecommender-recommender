package com.wesleykerr.steam.persistence.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import com.google.common.collect.Lists;
import com.wesleykerr.steam.persistence.dao.PlayerURLsDAO;

public class PlayerURLsDAOImpl implements PlayerURLsDAO {
    
    private Connection conn;

    public PlayerURLsDAOImpl(Connection conn) { 
        this.conn = conn;
    }
    
    @Override
    public void add(String playerURL) {
        try (Statement s = conn.createStatement()) { 
            int count = s.executeUpdate("insert into game_recommender.player_urls " +
                    "(player_url) values ('" + playerURL + "')");
        } catch (Exception e) { 
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String playerURL) {
        try (Statement s = conn.createStatement()) { 
            int count = s.executeUpdate("delete from game_recommender.player_urls " +
                    "where player_url = '" + playerURL + "'");
        } catch (Exception e) { 
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> fetch(int count) {
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("select player_url " +
            		"from game_recommender.player_urls limit " + count)) {

            List<String> urls = Lists.newArrayList();
            while (rs.next()) { 
                urls.add(rs.getString(1));
            }
            return urls;
        } catch (Exception e) { 
            throw new RuntimeException(e);
        }
    }

}
