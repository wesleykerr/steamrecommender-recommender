package com.wesleykerr.recommender;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wesleykerr.generated.MatrixProtos;
import com.wesleykerr.generated.MatrixProtos.Matrix;
import com.wesleykerr.generated.MatrixProtos.Matrix.Builder;
import com.wesleykerr.recommender.utils.RecommMatrix;
import com.wesleykerr.steam.gson.GameStats;
import com.wesleykerr.steam.gson.Player;

public abstract class ItemItemCF {
	protected static final Gson gson = new Gson();
	private static final Logger LOGGER = LoggerFactory.getLogger(ItemItemCF.class);

	protected static int LOG_RECORDS = 1000;
	protected RecommMatrix matrix;

    public ItemItemCF() {
    	matrix = new RecommMatrix();
    }
    
    /**
     * Observe the items in the map.
     * @param item
     * @param score
     */
    public abstract void observe(Map<Long,Double> wItems);
    
    /**
     * return the keys that we need for output.
     * @return
     */
    public abstract List<Long> getKeys();
    
    /**
     * Save the learned matrix to file.
     * @param file - the file to write the results to.
     * @throws Exception
     */
    public void output(String file) throws Exception { 
    	List<Long> keys = getKeys();
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
        		out.write("," + matrix.get(row,col));
    		}
    		out.write("\n");
    	}
    	out.close();
    }

    /**
     * 
     * @param file
     * @param byColumn
     * @throws Exception
     */
    public void saveMatrixPb(String file, boolean byColumn) throws Exception { 
    	saveMatrixPB(file, getKeys(), matrix, byColumn);
    }
    
    /**
     * 
     * @param file
     * @param table
     * @param byColumn
     * @throws Exception
     */
    public static void saveMatrixPB(String file, List<Long> keys, 
    		RecommMatrix table, boolean byColumn) throws Exception { 
    	
    	Builder builder = MatrixProtos.Matrix.newBuilder();
    	builder.setRowCount(keys.size());
    	builder.setColCount(keys.size());
    	builder.addAllRowNames(keys);
    	builder.addAllColNames(keys);
    	for (Long k1 : keys) { 
    		for (Long k2 : keys) { 
    			if (byColumn)
    				builder.addData(table.get(k2,k1));
    			else
    				builder.addData(table.get(k1,k2));
    		}
    	}
    	Matrix m = builder.build();
    	FileOutputStream out = new FileOutputStream(file);
    	m.writeTo(out);
    }
    
    /**
     * Load in the compressed data and observe each record with the emitter
     * @param file - the file we want to process
     * @param emitter  the value associated with each observation
     * @throws Exception
     */
    public void loadCompressed(String input, RewardEmitter emitter) throws Exception { 
    	BufferedReader in = null;
    	try { 
    		InputStream fileStream = new FileInputStream(input);
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
    				emitter.observe(this, p);
    				
    				++count;
    				if (count % LOG_RECORDS == 0)
    					LOGGER.info("Observed " + count + " players");
    			} catch (JsonSyntaxException e) { 
    				LOGGER.error("malformed json: " + tokens[1]);
    			}
        	}
        	in.close();
    	} finally { 
    		if (in != null)
    			in.close();
    	}    
    }
    
    static interface RewardEmitter { 
    	public void observe(ItemItemCF cf, Player p);
    }
    
    static class BinaryEmitter implements RewardEmitter { 
    	public void observe(ItemItemCF cf, Player p) {
    		Map<Long,Double> items = Maps.newTreeMap();
    		for (GameStats gameStats : p.getGames()) { 
    			// you have to play the game for at least 
    			// 20 minutes before it can be considered played.
    			if (gameStats.getCompletePlaytime() > 20) 
    				items.put(gameStats.getAppid(), 1.0);
    		}
    		cf.observe(items);
    	}
    }
}