package com.wesleykerr.steam.etl;

import static org.junit.Assert.assertNull;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.domain.player.FriendsList.Relationship;
import com.wesleykerr.steam.persistence.dao.CounterDAO;
import com.wesleykerr.steam.persistence.memory.CounterDAOImpl;

public class SteamAPITest {

    private CounterDAO counterDAO;
    private QueryDocument queryDocument;
    
    @Before
    public void init() { 
        counterDAO = new CounterDAOImpl();
        queryDocument = new QueryDocument(counterDAO);

//        try { 
//            InputStream input = new FileInputStream("config/recommender.properties");
//            Properties prop = new Properties();
//            prop.load(input);
//            System.setProperty("steam.key", prop.getProperty("steamKey"));
//        } catch (Exception e) { 
//            throw new RuntimeException(e);
//        }
    }
    
    @Test
    public void testGatherOwnedGames() { 
        
    }
    
    @Test
    @Ignore
    public void testPrivateFriendsList() { 
        long steamid = 76561198091316831L;
        
        SteamAPI api = new SteamAPI(queryDocument);
        List<Relationship> friendsList = api.gatherFriends(steamid);
        assertNull(friendsList);
    }
    
    @Test
    @Ignore
    public void testFriendsList() {
        long steamid = 76561197960435530L;
        SteamAPI api = new SteamAPI(queryDocument);
        List<Relationship> friendsList = api.gatherFriends(steamid);
        
    }
}
