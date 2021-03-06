package com.wesleykerr.steam.scraping;

import java.sql.Connection;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.domain.player.FriendsList;
import com.wesleykerr.steam.domain.player.FriendsList.Relationship;
import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.domain.player.Player.Builder;
import com.wesleykerr.steam.etl.SteamAPI;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.persistence.dao.CounterDAO;
import com.wesleykerr.steam.persistence.dao.SteamFriendsDAO;
import com.wesleykerr.steam.persistence.dao.SteamPlayerDAO;
import com.wesleykerr.steam.persistence.memory.CounterDAOImpl;
import com.wesleykerr.steam.persistence.sql.SteamFriendsDAOImpl;
import com.wesleykerr.steam.persistence.sql.SteamPlayerDAOImpl;
import com.wesleykerr.utils.Utils;

public class FriendsCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(FriendsCollector.class);

    private static int NUM_BATCHES = 10;
    private static int BATCH_SIZE = 100;

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
            LOGGER.info("Player: " + player.getSteamId());
            long millis = System.currentTimeMillis();
            List<Relationship> friends = steamAPI.gatherFriends(player.getSteamId());

            Player updated = Builder.create()
                    .withPlayer(player)
                    .withRevision(player.getRevision()+1)
                    .withLastUpdatedFriends(millis)
                    .build();
            steamPlayerDAO.update(updated);
            if (steamFriendsDAO.exists(player.getSteamId())) {
                LOGGER.info("... " + player.getLastUpdatedFriends());
                continue;
            }

            // TODO: send this update through some event system to that it is
            // persisted to the master dataset (since it is a change).  It will
            // be eventually persisted with this model when we update the player's games
            if (friends != null) { 
                FriendsList friendsList = FriendsList.Builder.create()
                        .withSteamId(player.getSteamId())
                        .withFriends(friends)
                        .withLastUpdated(System.currentTimeMillis())
                        .withRevision(1)
                        .build();
                
                boolean newFriends = steamFriendsDAO.add(friendsList);
                if (newFriends) { 
                    newFriendsCounter.incrCounter();
                }
                
                for (Relationship r : friends) { 
                    long steamId = Long.parseLong(r.getSteamid());
                    if (!steamPlayerDAO.exists(steamId)) {
                        steamPlayerDAO.addSteamId(steamId);
                        playerCounterDAO.incrCounter();
                    }
                    operationsCounter.incrCounter();
                }
            }
            
            if (friends == null && !player.isPrivate()) { 
                LOGGER.error("Player is visible, but has no friends (should be private)");
            }
            
            Utils.delay(random.nextInt(5)*100 + 500);
        }
    }
    
    public static void main(String[] args) throws Exception { 
        if (args.length != 2) {
            System.out.println("Usage: FriendsCollector <batch-size> <num-batches>");
            System.exit(0);
        }
        BATCH_SIZE = Integer.parseInt(args[0]);
        NUM_BATCHES = Integer.parseInt(args[1]);

        MySQL mySQL = MySQL.getDatabase("config/mysql-lh.properties");
        try { 
            Connection conn = mySQL.getConnection();
            SteamPlayerDAO steamPlayerDAO = new SteamPlayerDAOImpl(conn);
            SteamFriendsDAO steamFriendsDAO = new SteamFriendsDAOImpl(conn);

            FriendsCollector collector = new FriendsCollector(steamPlayerDAO, steamFriendsDAO);
            collector.run();

        } finally { 
            if (mySQL != null)
                mySQL.disconnect();
        }
    }
}
