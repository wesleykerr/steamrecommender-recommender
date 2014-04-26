package com.wesleykerr.steam.etl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.domain.game.Game;
import com.wesleykerr.steam.domain.game.GameplayStats;
import com.wesleykerr.steam.domain.game.GameplayStats.Builder;
import com.wesleykerr.steam.domain.player.GameStats;
import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.persistence.dao.GamesDAO;
import com.wesleykerr.steam.persistence.sql.GamesDAOImpl;
import com.wesleykerr.utils.GsonUtils;

public class UpdateSteamDataset {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSteamDataset.class);

    private static final String STORE_URL = "http://store.steampowered.com/app/";
    private static final String IMAGE_URL = "http://cdn2.steampowered.com/v/gfx/apps/%d/header_292x136.jpg";
    private static final String DEFAULT_IMG_URL = "http://www.steamrecommender.com/img/applogo.gif";

	private Map<Long,DescriptiveStatistics> playtimeMap;
	private Map<Long,GameplayStats.Builder> gameStats;
	
	/**
	 * 
	 * @param input
	 * @throws Exception
	 */
	public void estimatePlaytime(String input) throws Exception { 
		playtimeMap = Maps.newHashMap();
		gameStats = Maps.newHashMap();
		
		File inputFile = new File(input);
		try (Reader reader = new FileReader(inputFile);
                BufferedReader in = new BufferedReader(reader)) {

			Gson gson = GsonUtils.getDefaultGson();
			while (in.ready()) { 
				Player p = gson.fromJson(in.readLine(), Player.class);
		        for (GameStats stats : p.getGames()) {
		        	Builder builder = gameStats.get(stats.getAppid());
		        	if (builder == null) {
		        		builder = Builder.create();
		        		gameStats.put(stats.getAppid(), builder);
		        	}
		        	builder.incrementOwned();
		        	
		        	if (stats.getCompletePlaytime() > 0) { 
		        		DescriptiveStatistics dstats = playtimeMap.get(stats.getAppid());
		        		if (dstats == null) { 
		        			dstats = new DescriptiveStatistics();
		        			playtimeMap.put(stats.getAppid(), dstats);
		        		}
		        		
		        		double hoursPlayed = stats.getCompletePlaytime() / 60.0;
		        		builder.incrementPlaytime(hoursPlayed);
		        		dstats.addValue(hoursPlayed);
		        	} else {
		        		builder.incrementNotPlayed();
		        	}
		        }
			}
		}
	}
	
	public void savePlaytimeDetails(String output) throws Exception { 
		File outputFile = new File(output);
		try (Writer writer = new FileWriter(outputFile);
				BufferedWriter out = new BufferedWriter(writer)) { 
			
			for (Map.Entry<Long,DescriptiveStatistics> entry : playtimeMap.entrySet()) { 
				
				DescriptiveStatistics stats = entry.getValue();
		        double quantile25 = stats.getPercentile(25);
		        double median = stats.getPercentile(50);
		        double quantile75 = stats.getPercentile(75);

		        StringBuilder buf = new StringBuilder();
		        buf.append(entry.getKey()).append("\t");
		        buf.append(String.format("%.2f", quantile25)).append("\t");
		        buf.append(String.format("%.2f", median)).append("\t");
		        buf.append(String.format("%.2f", quantile75)).append("\n");
		        out.write(buf.toString());
			}
		}
	}
	
	/**
	 * Deploy playtime details to the steamrecommender.com game database.
	 * @param gamesDAO
	 * @throws Exception
	 */
	public void pushPlaytimeDetails(GamesDAO gamesDAO) throws Exception { 
	    for (Map.Entry<Long,DescriptiveStatistics> entry : playtimeMap.entrySet()) { 
	        DescriptiveStatistics stats = entry.getValue();
	        Builder builder = gameStats.get(entry.getKey())
	                .withMedianPlaytime(stats.getPercentile(50))
	                .withQ25Playtime(stats.getPercentile(25))
	                .withQ75Playtime(stats.getPercentile(75));

	        GameplayStats gameStats = builder.build();
	        gamesDAO.updateOrAddStats(entry.getKey(), gameStats);
	    }
	}
	
	/**
	 * Check the game url and image url to see if we need to put in a 
	 * default or not.
	 * @param gamesDAO
	 * @param userAgent
	 * @throws Exception
	 */
	public void updateSteamUrls(GamesDAO gamesDAO, String userAgent) throws Exception { 
	    QueryDocument queryDoc = new QueryDocument();
        List<Game> games = gamesDAO.getGamesForImageUpdate();
        for (Game game : games)  {
            LOGGER.info("Game " + game.getAppid() + " : " + game.getTitle());
            if (queryDoc.checkIfExists(STORE_URL+game.getAppid(), userAgent, 2)) {
                game.setSteamURL(STORE_URL + game.getAppid());
            }
            
            String url = String.format(IMAGE_URL, game.getAppid());
            if (queryDoc.checkIfExists(url, userAgent, 2)) { 
                game.setSteamImgURL(url);
            } else { 
                game.setSteamImgURL(DEFAULT_IMG_URL);
            }
            gamesDAO.updateUrlDetails(game);
        }
        LOGGER.info("Updated " + games.size() + " games");
	}
	
    public static Options getOptions() { 
    	Options options = new Options();
    	
    	@SuppressWarnings("static-access")
		Option input = OptionBuilder
    			.withLongOpt("input")
    			.withArgName("input")
    			.hasArg()
    			.isRequired()
    			.create("i");
    	options.addOption(input);

    	@SuppressWarnings("static-access")
		Option output = OptionBuilder
    			.withLongOpt("output")
    			.withArgName("output")
    			.hasArg()
    			.isRequired()
    			.create("o");
    	options.addOption(output);

    	@SuppressWarnings("static-access")
    	Option help = OptionBuilder
    			.withLongOpt("help")
    			.withArgName("help")
    			.create("h");
    	options.addOption(help);

    	return options;
    }
    
    public static void printHelp(Options options) { 
    	HelpFormatter formatter = new HelpFormatter();
    	formatter.printHelp( "UpdateSteamDataset", options );
    }
	
	public static void main(String[] args) throws Exception { 
	    if (args.length != 1) {
	        LOGGER.error("Usage: UpdateSteamDataset daily/weekly");
	        return;
	    }
	    
        File lockFile = new File("/tmp/UpdateSteamDataset.lock");
        if (lockFile.exists()) { 
            LOGGER.info("Process already running [" + lockFile.toString() + "]");
            throw new RuntimeException("Process already running!");
        }
        lockFile.createNewFile();
        lockFile.deleteOnExit();
	    
        MySQL mysql = MySQL.getDreamhost();
        GamesDAO gamesDAO = new GamesDAOImpl(mysql.getConnection());

        UpdateSteamDataset dataset = new UpdateSteamDataset();
        if ("daily".equals(args[0])) { 
            dataset.updateSteamUrls(gamesDAO, "Steam Recommender");
        } else { 
        	Options options = getOptions();
        	CommandLineParser parser = new BasicParser();
        	
            String input = "/data/steam/training-data.gz";
            String output = "data/steam/playtime";
        	try {
        		CommandLine line = parser.parse(options, args);
        		if (line.hasOption("h")) {
        			printHelp(options);
        			System.exit(1);
        		}
        		
        		if (line.hasOption("i")) 
        			input = line.getOptionValue("i");
        		
        		if (line.hasOption("o")) 
        			output = line.getOptionValue("o");
        	} catch (ParseException exp) { 
        		printHelp(options);
        		System.exit(1);
        	}
        	
            
            dataset.estimatePlaytime(input);
            dataset.pushPlaytimeDetails(gamesDAO);
            dataset.savePlaytimeDetails(output);
        }
	}
}
