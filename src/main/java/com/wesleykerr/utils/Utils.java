package com.wesleykerr.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class Utils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	public static Map<Long,double[]> loadGamePlaytime(File inputFile, String statsType) 
	        throws Exception { 
	    
        LOGGER.info("Loading " + inputFile.toString());
        Map<Long,double[]> quantileMap = Maps.newHashMap();
        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) { 
            while (in.ready()) {
                String line = in.readLine();
                String[] tokens = line.split("\t");
                if (tokens.length != 5) {
                    LOGGER.error("Unknown number of tokens: " + line);
                    throw new RuntimeException("Unknown number of tokens: " + line);
                }
                
                if (!statsType.equals(tokens[1])) 
                    continue;
                
                Long appId = Long.parseLong(tokens[0]);
                if (quantileMap.containsKey(appId)) { 
                    LOGGER.error("Processing the same row twice..." + appId);
                    throw new RuntimeException("Processing " + appId + " twice");
                }
                
                double[] quantiles = new double[3];
                quantiles[0] = Double.parseDouble(tokens[2]);
                quantiles[1] = Double.parseDouble(tokens[3]);
                quantiles[2] = Double.parseDouble(tokens[4]);
                quantileMap.put(appId, quantiles);
            }
        } 
        LOGGER.info("Loading " + inputFile.toString() + " finished");
	    return quantileMap;
	}
	
	/**
	 * Bolierplate code for sleep
	 * @param millis
	 */
	public static void delay(long millis) { 
		try { 
			Thread.sleep(millis);
		} catch (Exception e) { 
			LOGGER.warn(e.getMessage());
		}
	}
}
