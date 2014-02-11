package com.wesleykerr.steam.etl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.wesleykerr.steam.domain.game.GameplayStats;
import com.wesleykerr.steam.domain.game.GameplayStats.Builder;
import com.wesleykerr.steam.domain.player.GameStats;
import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.utils.GsonUtils;

public class SteamPlaytimeDataset {
	
	private Map<Long,DescriptiveStatistics> playtimeMap;
	private Map<Long,GameplayStats.Builder> gameStats;
	

	public void extractFromMySQL(String dataFile) throws Exception { 
    	MySQL mysql = MySQL.getDreamhost();
    	
    	File output = new File(dataFile);    	
    	try (Writer writer = new FileWriter(output);
    			BufferedWriter bufWriter = new BufferedWriter(writer)) { 
        	mysql.streamResultSet(QUERY, bufWriter);
    	}
	}
	
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
	
	public static void main(String[] args) throws Exception { 
		SteamPlaytimeDataset dataset = new SteamPlaytimeDataset();
		String file = "/data/steam/training-data";
		
		dataset.extractFromMySQL(file);
		dataset.estimatePlaytime(file);
		dataset.savePlaytimeDetails("/data/steam/playtime");
	}
	
	private static final String QUERY = 
			"select content from steam_data.players "
			+ "where num_games > 0 and private = 0 and revision > 0 "
			+ "limit 5000";
}
