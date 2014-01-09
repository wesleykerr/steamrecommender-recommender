package com.wesleykerr.steam.scraping;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.domain.player.FriendsList;
import com.wesleykerr.steam.domain.player.FriendsList.Relationship;
import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.domain.player.Player.Builder;
import com.wesleykerr.steam.etl.SteamAPI;
import com.wesleykerr.steam.persistence.Couchbase;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.persistence.dao.CounterDAO;
import com.wesleykerr.steam.persistence.dao.SteamFriendsDAO;
import com.wesleykerr.steam.persistence.dao.SteamPlayerDAO;
import com.wesleykerr.steam.persistence.memory.CounterDAOImpl;
import com.wesleykerr.steam.persistence.nosql.SteamFriendsDAOImpl;
import com.wesleykerr.steam.persistence.nosql.SteamPlayerDAOImpl;
import com.wesleykerr.utils.GsonUtils;
import com.wesleykerr.utils.Utils;

public class FriendsCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(FriendsCollector.class);
    private static final String HOST = "api.steampowered.com";

    private static final int NUM_BATCHES = 10;
    private static final int BATCH_SIZE = 100;

    private QueryDocument queryDocument;

    private SteamPlayerDAO steamPlayerDAO;
    private SteamFriendsDAO steamFriendsDAO;

    private CounterDAO counterDAO;
    private CounterDAO playerCounterDAO;
    private CounterDAO operationsCounter;
    private CounterDAO newFriendsCounter;

    public FriendsCollector(SteamPlayerDAO steamPlayerDAO, SteamFriendsDAO steamFriendsDAO) 
            throws Exception { 
        this.steamPlayerDAO = steamPlayerDAO;
        this.steamFriendsDAO = steamFriendsDAO;
        
        playerCounterDAO = new CounterDAOImpl();
        operationsCounter = new CounterDAOImpl();
        newFriendsCounter = new CounterDAOImpl();

        counterDAO = new CounterDAOImpl();
        queryDocument = new QueryDocument(counterDAO);
    }

    public void run() { 
        for (int i = 0; i < NUM_BATCHES; ++i) { 
            LOGGER.info("batch " + i);
            runBatch(BATCH_SIZE);
            Utils.delay(10000);
        }
        
        LOGGER.info("Pulled " + (NUM_BATCHES*BATCH_SIZE) + " friends lists");
        LOGGER.info("Added " + playerCounterDAO.getCounter() + " new accounts");
        LOGGER.info("Checked " + operationsCounter.getCounter() + "  accounts");
        LOGGER.info("Added " + newFriendsCounter.getCounter() + "  new friends");
    }
    
    public void runBatch(int batchSize) { 
        Random random = new Random();
        List<Player> steamIds = steamPlayerDAO.getSteamIdsWithNoFriends(batchSize);
        
        SteamAPI steamAPI = new SteamAPI(queryDocument);
        for (Player player : steamIds) { 
            LOGGER.info("Player: " + player.getId());
            long steamId = Long.parseLong(player.getId());
            List<Relationship> friends = steamAPI.gatherFriends(steamId);

            Player updated = Builder.create()
                    .withPlayer(player)
                    .withFriendsMillis(System.currentTimeMillis())
                    .build();
            String updatedDocument = GsonUtils.getDefaultGson().toJson(updated);
            steamPlayerDAO.update(updated.getId(), updatedDocument);
//            LOGGER.info("Updated: " + updatedDocument);

            if (steamFriendsDAO.exists(player.getId())) {
                LOGGER.info("... " + player.getFriendsMillis());
                continue;
            }

            // TODO: send this update through some event system to that it is
            // persisted to the master dataset (since it is a change).  It will
            // be eventually persisted with this model when we update the player's games
            if (friends != null) { 
                FriendsList friendsList = FriendsList.Builder.create()
                        .withId(player.getId())
                        .withFriends(friends)
                        .withUpdateDateTime(System.currentTimeMillis())
                        .build();
                
                boolean newFriends = steamFriendsDAO.add(friendsList);
                if (newFriends) { 
                    newFriendsCounter.incrCounter();
                }
                
                for (Relationship r : friends) { 
                    boolean added = steamPlayerDAO.add(Long.parseLong(r.getSteamid()));
                    operationsCounter.incrCounter();
                    if (added) 
                        playerCounterDAO.incrCounter();
                }
            }
            
            if (friends == null && player.isVisible()) { 
                LOGGER.error("Player is visible, but has no friends (should be private)");
            }
            
            Utils.delay(random.nextInt(5)*100 + 500);
        }
    }
    
    public static void main(String[] args) throws Exception { 
        CouchbaseClient defaultClient = null;
        CouchbaseClient friendsClient = null;
        MySQL mySQL = null;
        try { 
            defaultClient = Couchbase.connect("default");
            SteamPlayerDAO steamPlayerDAO = new SteamPlayerDAOImpl(defaultClient);

            friendsClient = Couchbase.connect("friends");
            SteamFriendsDAO steamFriendsDAO = new SteamFriendsDAOImpl(friendsClient);

            FriendsCollector collector = new FriendsCollector(steamPlayerDAO, steamFriendsDAO);
            collector.run();

        } finally { 
            if (mySQL != null)
                mySQL.disconnect();
            if (defaultClient != null) 
                defaultClient.shutdown();
            if (friendsClient != null) 
                friendsClient.shutdown();
        }
    }
}
