package com.wesleykerr.steam.etl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.domain.player.GameStats;
import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.domain.player.Player.Builder;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.persistence.dao.CounterDAO;
import com.wesleykerr.steam.persistence.dao.SteamPlayerDAO;
import com.wesleykerr.steam.persistence.memory.CounterDAOImpl;
import com.wesleykerr.steam.persistence.sql.SteamPlayerDAOImpl;

public class UpdatePlayers {
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdatePlayers.class);
	
    private static int BATCH_SIZE = 100;
	private static int NUM_BATCHES = 10;

	private long millis;
	
	private QueryDocument queryDoc;
	private SteamAPI info;
	
	public UpdatePlayers() throws Exception { 
        GregorianCalendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		millis = today.getTime().getTime();

		CounterDAO counter = new CounterDAOImpl();
		queryDoc = new QueryDocument(counter);
		info = new SteamAPI(queryDoc);
	}
	
	public void runBatch(List<Player> players, SteamPlayerDAO playerDAO) throws Exception { 
        for (Player p : players) { 
            List<GameStats> list = info.gatherOwnedGames(p.getSteamId());
            Builder builder = Builder.create()
                    .withPlayer(p)
                    .withRevision(p.getRevision()+1)
                    .withGames(list)
                    .isPrivate(list == null)
                    .withLastUpdated(millis);
            Player updated = builder.build();
            LOGGER.info("query player " + updated.getSteamId());
            
            playerDAO.update(updated);
            Thread.currentThread().sleep(900);
            
        }
	}

	public void run(String viewName) throws Exception { 
	    MySQL mySQL = MySQL.getDreamhost();
        
	    SteamPlayerDAO playerDAO = new SteamPlayerDAOImpl(mySQL.getConnection());
	    try { 
	        Random r = new Random();
	        for (int i = 0; i < NUM_BATCHES; ++i) {
	            List<Player> players = Lists.newArrayList();
	            if ("new".equals(viewName))
	                players = playerDAO.getNewPlayers(BATCH_SIZE);
	            else if ("refresh".equals(viewName))
	                players = playerDAO.getRefreshList(BATCH_SIZE);
	            runBatch(players, playerDAO);
	            
	            Thread.currentThread().sleep(5000);
	            LOGGER.debug("finished batch " + i);
	            if (i % 10 == 0) {
                    Thread.currentThread();
                    Thread.sleep(1000*r.nextInt(30));
                }
	        }
	    } finally {
	        playerDAO.close();
	        mySQL.disconnect();
	    }
	    
	}
	
	public static void main(String[] args) throws Exception { 
		// "week_old_players" -- "new_players"
		if (args.length != 3) {
			System.out.println("Usage: UpdatePlayers <view-name> <batch-size> <num-batches>");
			System.exit(0);
		}
		String view = args[0];
		BATCH_SIZE = Integer.parseInt(args[1]);
		NUM_BATCHES = Integer.parseInt(args[2]);
		
		// check to see if we are already running...
		File lockFile = new File("/tmp/UpdatePlayers.lock");
		if (lockFile.exists()) { 
			LOGGER.info("Process already running [" + lockFile.toString() + "]");
			throw new RuntimeException("Process already running!");
		}
		lockFile.createNewFile();
		lockFile.deleteOnExit();

		Properties prop = new Properties();
		// TODO make this a parameter that is passed in.
		InputStream input = new FileInputStream("config/recommender.properties");
		prop.load(input);
		System.setProperty("steam.key", prop.getProperty("steamKey"));

		UpdatePlayers up = new UpdatePlayers();
		up.run(view);
	}
}
