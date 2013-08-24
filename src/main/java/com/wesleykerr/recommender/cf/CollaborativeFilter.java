/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wesleykerr.recommender.cf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wesleykerr.steam.gson.GameStats;
import com.wesleykerr.steam.gson.Player;

/**
 *
 * @author wkerr
 */
public class CollaborativeFilter {
	private static final Gson gson = new Gson();
	private static final Logger LOGGER = LoggerFactory.getLogger(CollaborativeFilter.class);

	private Set<Long> allItems;
	
	/** Using a table allows us to have rows/columns that
	 *  are non-zero based.  
	 */
	private Table<Long,Long,Double> matrix;

    public CollaborativeFilter() {
    	matrix = TreeBasedTable.create();
    	allItems = new TreeSet<>();
    }
    
    /**
     * Increment the value in the table by a given amount.
     * @param row
     * @param col
     * @param v
     */
    private void incTable(long row, long col, double v) {
    	matrix.put(row, col, v + get(row, col));
    }
    
    /**
     * Grab the value from the matrix.  If non-existent, then
     * return a default value.
     * @param row
     * @param col
     * @return
     */
    public double get(long row, long col) { 
		Double value = matrix.get(row, col);
		return value == null ? 0.0 : value;
    }
    
    /**
     * Observe the items in the map.
     * @param item
     * @param score
     */
    public void observe(Map<Long,Double> items) { 
    	List<Long> itemList = Lists.newArrayList(items.keySet());
    	for (int i = 0; i < itemList.size(); ++i) { 
    		Long key1 = itemList.get(i);
    		incTable(key1, key1, items.get(key1));

    		for (int j = i+1; j < items.size(); ++j) {
    			Long key2 = itemList.get(j);
    			
    			double sumRating = items.get(key1) + items.get(key2);
    			incTable(key1, key2, sumRating);
    			incTable(key2, key1, sumRating);
    		}
    		
    		allItems.add(key1);
    	}
    }
    
    /**
     * Observe is used when we are talking about items that are either
     * interacted with or they are not.  If they user interacted with an item 
     * then it is part of the list of items.  The user id is left off since
     * it is not necessary for this item-item collaborative filter.
     * @param items
     */
    public void observe(List<Long> items) { 
    	for (int i = 0; i < items.size(); ++i) { 
    		Long key1 = items.get(i);
    		incTable(key1, key1, 1);

    		for (int j = i+1; j < items.size(); ++j) {
    			Long key2 = items.get(j);
    			
    			incTable(key1, key2, 1);
    			incTable(key2, key1, 1);
    		}
    		
    		allItems.add(key1);
    	}
    }
    
    public void computeCosine() { 
    	List<Long> keys = new ArrayList<>(allItems);
    	Table<Long,Long,Double> cosMatrix = TreeBasedTable.create();
    	for (Long row : keys) {
    		for (Long col : keys) {
    			double bothCount = get(row, col);
    			double rowCount = get(row, row);
    			double colCount = get(col, col);
    			double cosSim = bothCount / (Math.sqrt(rowCount)*Math.sqrt(colCount));
    			
    			cosMatrix.put(row, col, cosSim);
    		}
    	}
    	matrix = cosMatrix;
    }
    
    public void rowNormalize() { 
    	List<Long> keys = new ArrayList<>(allItems);
    	for (Long row : keys) { 
    		double sum = 0.0;
    		for (Long col : keys) { 
    			sum += get(row, col);
    		}
    		
    		for (Long col : keys) { 
    			matrix.put(row, col, get(row, col) / sum);
    		}
    	}
    }
    
    public void output(String file) throws Exception { 
    	List<Long> keys = new ArrayList<>(allItems);
    	BufferedWriter out = new BufferedWriter(new FileWriter(file));

    	out.write("item");
    	for (Long key : keys) {
    		out.write(",");
    		out.write(Long.toString(key));
    	}
    	out.write("\n");

    	for (Long col : keys) {
    		out.write(Long.toString(col));
    		for (Long row : keys) {
        		out.write("," + get(row,col));
    		}
    		out.write("\n");
    	}
    	out.close();
    }
    
    public static void main(String[] args) throws Exception {     	
    	CollaborativeFilter cf = new CollaborativeFilter();
    	RewardEmitter emitter = new BinaryEmitter();

    	BufferedReader in = null;
    	try { 
    		String file = "/data/steam/training-data.gz";
    		InputStream fileStream = new FileInputStream(file);
    		InputStream gzipStream = new GZIPInputStream(fileStream);
    		Reader decode = new InputStreamReader(gzipStream, "UTF-8");
        	in = new BufferedReader(decode);
        	
        	int count = 0;
        	while (in.ready()) { 
        		String[] tokens = in.readLine().split("\t");
        		if (tokens.length != 2)
        			throw new RuntimeException("UNKNOWN");
        		
    			try { 
    				Player p = gson.fromJson((String) tokens[1].toString(), Player.class);
    				emitter.observe(cf, p);
    				
    				++count;
    				if (count % 10000 == 0)
    					LOGGER.debug("Observed " + count + " players");
    			} catch (JsonSyntaxException e) { 
    				LOGGER.error("malformed json: " + tokens[1]);
    			}
        	}
        	in.close();
        	cf.computeCosine();
        	cf.rowNormalize();
        	cf.output("/data/steam/item_item.csv");
        	LOGGER.info("total number of items " + cf.allItems.size());
    	} finally { 
    		if (in != null)
    			in.close();
    	}
    	
    }
    
    static interface RewardEmitter { 
    	public void observe(CollaborativeFilter cf, Player p);
    }
    
    static class BinaryEmitter implements RewardEmitter { 
    	public void observe(CollaborativeFilter cf, Player p) {
    		List<Long> items = Lists.newArrayList();
    		for (GameStats gameStats : p.getGames()) { 
    			if (gameStats.getCompletePlaytime() > 0) 
    				items.add(gameStats.getAppid());
    		}
    		cf.observe(items);
    	}
    }
}
