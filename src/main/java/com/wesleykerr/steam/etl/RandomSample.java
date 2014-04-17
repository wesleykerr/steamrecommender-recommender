package com.wesleykerr.steam.etl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.domain.player.GameStats;
import com.wesleykerr.steam.domain.player.Player.Builder;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.persistence.dao.CounterDAO;
import com.wesleykerr.steam.persistence.dao.SteamPlayerSampleDAO;
import com.wesleykerr.steam.persistence.memory.CounterDAOImpl;
import com.wesleykerr.steam.persistence.sql.SteamPlayerSampleDAOImpl;

public class RandomSample {
	private static final Logger LOGGER = LoggerFactory.getLogger(RandomSample.class);
	
    private static int BATCH_SIZE = 5;
	private static int NUM_BATCHES = 2;

	private static final long START_ID = 76561197960265729L;
	private static final int NUM_USERS = 172000000;

	private Calendar date;
	
	private QueryDocument queryDoc;
	private SteamAPI info;
	
	public RandomSample() throws Exception { 
        date = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

		CounterDAO counter = new CounterDAOImpl();
		queryDoc = new QueryDocument(counter);
		info = new SteamAPI(queryDoc);
	}

	/**
	 * Random select a steam id, gather it's details and 
	 * insert it into the database.
	 * @param r
	 * @param playerDAO
	 * @throws Exception
	 */
	public void addSteamId(Random r, SteamPlayerSampleDAO playerDAO)
			throws Exception { 
		long steamId = START_ID + r.nextInt(NUM_USERS);
        List<GameStats> list = info.gatherOwnedGames(steamId);
        Builder builder = Builder.create()
        		.withSteamId(steamId)
                .withRevision(1)
                .withGames(list)
                .withNumGames(list == null ? 0 : list.size())
                .isPrivate(list == null)
                .withLastUpdated(date.getTimeInMillis());
        playerDAO.add(builder.build(), date);
        Thread.currentThread().sleep(900);
	}

	public void run() throws Exception { 
	    MySQL mySQL = MySQL.getDatabase("config/mysql-lh.properties");
	    SteamPlayerSampleDAO playerDAO = new SteamPlayerSampleDAOImpl(
	    		mySQL.getConnection());
	    try { 
	        Random r = new Random();
	        for (int i = 0; i < NUM_BATCHES; ++i) {
	        	for (int j = 0; j < BATCH_SIZE; ++j) { 
	        		addSteamId(r, playerDAO);
	        	}
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
		if (args.length != 2) {
			System.out.println("Usage: UpdatePlayers <batch-size> <num-batches>");
			System.exit(0);
		}
		BATCH_SIZE = Integer.parseInt(args[0]);
		NUM_BATCHES = Integer.parseInt(args[1]);
		
		// check to see if we are already running...
		File lockFile = new File("/tmp/RandomSample.lock");
		if (lockFile.exists()) { 
			LOGGER.info("Process already running [" + lockFile.toString() + "]");
			throw new RuntimeException("Process already running!");
		}
		lockFile.createNewFile();
		lockFile.deleteOnExit();

		Properties prop = new Properties();
		// TODO(wkerr) make this a parameter that is passed in.
		InputStream input = new FileInputStream("config/recommender.properties");
		prop.load(input);
		System.setProperty("steam.key", prop.getProperty("steamKey"));

		RandomSample up = new RandomSample();
		up.run();
	}
}
