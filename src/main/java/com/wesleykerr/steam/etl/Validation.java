package com.wesleykerr.steam.etl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wesleykerr.steam.domain.player.GameStats;
import com.wesleykerr.steam.domain.player.Player;

public class Validation {
	private static final Logger LOGGER = LoggerFactory.getLogger(Validation.class);
	protected static final Gson gson = new Gson();
	
	public static final long appid = 440L;
	
	public static void main(String[] args) throws Exception { 
    	BufferedReader in = null;
    	BufferedWriter out1 = null;
    	BufferedWriter out2 = null;
    	try { 
    		out1 = new BufferedWriter(new FileWriter("/tmp/validation-" + appid + "-total"));
    		out2 = new BufferedWriter(new FileWriter("/tmp/validation-" + appid + "-recent"));

    		InputStream fileStream = new FileInputStream("/data/steam/training-data.gz");
    		InputStream gzipStream = new GZIPInputStream(fileStream);
    		Reader decode = new InputStreamReader(gzipStream, "UTF-8");
        	in = new BufferedReader(decode);
        	
        	while (in.ready()) { 
        		String[] tokens = in.readLine().split("\t");
        		if (tokens.length != 2)
        			throw new RuntimeException("UNKNOWN");
        		
    			try { 
    				Player p = gson.fromJson((String) tokens[1].toString(), Player.class);
    				for (GameStats stats : p.getGames()) { 
    					if (stats.getAppid() == appid) {
    						if (stats.getRecentPlaytime() > 0)
    							out2.write(stats.getRecentPlaytime()/60.0 + "\n");
    						if (stats.getCompletePlaytime() > 0) 
    							out1.write(stats.getCompletePlaytime()/60.0 + "\n");
    					}
    				}
    			} catch (JsonSyntaxException e) { 
    				LOGGER.error("malformed json: " + tokens[1]);
    			}
        	}
    	} finally { 
    		if (in != null)
    			in.close();
    		
    		if (out1 != null)
    			out1.close();
    		if (out2 != null)
    			out2.close();
    	}    
    }
}
