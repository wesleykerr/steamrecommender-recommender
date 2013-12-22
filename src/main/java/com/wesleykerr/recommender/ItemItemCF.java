package com.wesleykerr.recommender;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wesleykerr.recommender.utils.MatrixOps;
import com.wesleykerr.recommender.utils.RecommMatrix;
import com.wesleykerr.steam.domain.player.GameStats;
import com.wesleykerr.steam.domain.player.Player;

public class ItemItemCF {
	protected static final Gson gson = new Gson();
	private static final Logger LOGGER = LoggerFactory.getLogger(ItemItemCF.class);

	protected static int LOG_RECORDS = 1000;

	protected RecommMatrix matrix;
	protected Set<Long> allItems;

    public ItemItemCF() {
    	matrix = new RecommMatrix();
    	allItems = Sets.newTreeSet();
    }
    
    /**
     * Observe the items in the map.
     * @param item
     * @param score
     */
    public void observe(Map<Long,Double> wItems) { 
        List<Long> items = Lists.newArrayList(wItems.keySet());
        Collections.sort(items);
        
        for (int i = 0; i < items.size(); ++i) { 
            Long key1 = items.get(i);
            Double value1 = wItems.get(key1);

            for (int j = i; j < items.size(); ++j) {
                Long key2 = items.get(j);
                Double value2 = wItems.get(key2);

                matrix.incTable(key1, key2, value1*value2);
                matrix.incTable(key2, key1, value1*value2);
            }
        }
        allItems.addAll(wItems.keySet());
    }
    
    /**
     * Save the learned matrix to file.
     * @param file - the file to write the results to.
     * @throws Exception
     */
    public static void output(String file, List<Long> items, RecommMatrix matrix) throws Exception { 
    	BufferedWriter out = new BufferedWriter(new FileWriter(file));
    	out.write("item");
    	for (Long key : items) {
    		out.write(",");
    		out.write(Long.toString(key));
    	}
    	out.write("\n");

    	for (Long col : items) {
    		out.write(Long.toString(col));
    		for (Long row : items) {
        		out.write("," + matrix.get(row,col));
    		}
    		out.write("\n");
    	}
    	out.close();
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
    				observe(emitter.emit(this, p));
    				    
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
    
    public static void main(String[] args) throws Exception {       
        ItemItemCF.LOG_RECORDS = 20000;
        ItemItemCF cf = new ItemItemCF();
        RewardEmitter emitter = new BinaryNormalizedEmitter();

        String input = "/data/steam/training-data.gz";
        String output = "/data/steam/heats.csv";
        
        cf.loadCompressed(input, emitter);
        RecommMatrix matrix = MatrixOps.cosine(Lists.newArrayList(cf.allItems), cf.matrix);
        
        output(output, Lists.newArrayList(cf.allItems), matrix);
        LOGGER.info("total number of items " + cf.allItems.size());
    }
    
    /**
     * The RewardEmitter is the one that emits all of the games
     * that the player has played.
     * @author wkerr
     *
     */
    static interface RewardEmitter { 
    	public Map<Long,Double> emit(ItemItemCF cf, Player p);
    }
    
    static class BinaryEmitter implements RewardEmitter { 
        @Override
    	public Map<Long,Double> emit(ItemItemCF cf, Player p) {
    		Map<Long,Double> items = Maps.newTreeMap();
    		for (GameStats gameStats : p.getGames()) { 
    			// you have to play the game for at least 
    			// 20 minutes before it can be considered played.
    			if (gameStats.getCompletePlaytime() > 20) 
    				items.put(gameStats.getAppid(), 1.0);
    		}
    		return items;
    	}
    }
    
    static class BinaryNormalizedEmitter extends BinaryEmitter { 
        @Override
        public Map<Long,Double> emit(ItemItemCF cf, Player p) {
            Map<Long,Double> items = super.emit(cf, p);
            Map<Long,Double> nItems = Maps.newTreeMap();
            for (Map.Entry<Long,Double> entry : items.entrySet()) 
                nItems.put(entry.getKey(), entry.getValue() / items.size());
            return nItems;
        }
    }

    
    static class ScoreEmitter implements RewardEmitter { 
        private Map<Long,Stats> statsMap;
        
        public void loadGameStats(String file) throws Exception { 
            statsMap = Maps.newHashMap();
            BufferedReader in = null;
            try { 
                in = new BufferedReader(new FileReader(file));
                for (String line = in.readLine(); line != null; line = in.readLine()) { 
                    String[] tokens = line.split("\t");
                    if ("recent".equals(tokens[1]))
                        continue;
                    
                    long appId = Long.parseLong(tokens[0]);
                    statsMap.put(appId, Stats.create(tokens[2], tokens[3], tokens[4]));
                }
            } finally { 
                if (in != null)
                    in.close();
            }
        }

        @Override
        public Map<Long,Double> emit(ItemItemCF cf, Player p) {
            Map<Long,Double> items = Maps.newTreeMap();
            for (GameStats gameStats : p.getGames()) { 
                // you haven't played the game if you played less than 20 minutes.
                if (gameStats.getCompletePlaytime() < 20)
                    continue;
                
                Stats s = statsMap.get(gameStats.getAppid());
                if (s == null) { 
                    LOGGER.error("Missing stats for a game played: " + gameStats.getAppid());
                    continue;
                }

                items.put(gameStats.getAppid(), getScore(gameStats.getCompletePlaytime(), s));
            }
            return items;
        }
        
        public double getScore(long minutesPlayed, Stats stats) { 
            double hoursPlayed = minutesPlayed / 60.0;
            if (hoursPlayed < stats.getQ25())
                return 1;
            if (hoursPlayed < stats.getMedian())
                return 2;
            if (hoursPlayed < stats.getQ75())
                return 3;
            return 4;
        }
    }
    
    public static class Stats { 
        private double median;
        private double q25;
        private double q75;

        /**
         * @return the median
         */
        public double getMedian() {
            return median;
        }
        /**
         * @return the q25
         */
        public double getQ25() {
            return q25;
        }
        /**
         * @return the q75
         */
        public double getQ75() {
            return q75;
        }
        
        public static Stats create(String q25, String median, String q75) { 
            Stats s = new Stats();
            s.q25 = Double.parseDouble(q25);
            s.median = Double.parseDouble(median);
            s.q75 = Double.parseDouble(q75);
            return s;
        }
    }    
}