package com.wesleykerr.recommender;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class HeatS extends ItemItemCF {
	private static final Logger LOGGER = LoggerFactory.getLogger(HeatS.class);

	private Map<Long,Double> itemTotals;
	
    public HeatS() {
    	super();
    	
    	itemTotals = Maps.newHashMap();
    }
    
    @Override
    public void observe(Map<Long,Double> wItems) { 
    	List<Long> itemList = Lists.newArrayList(wItems.keySet());
    	double total = 0.0;
    	for (Map.Entry<Long, Double> entry : wItems.entrySet()) {
    		total += entry.getValue();
    		
    		Double itemTotal = itemTotals.get(entry.getKey());
    		if (itemTotal == null)
    			itemTotal = 0.0;
    		itemTotals.put(entry.getKey(), itemTotal + entry.getValue());
    	}
    	
    	for (int i = 0; i < itemList.size(); ++i) { 
    		Long key1 = itemList.get(i);
    		matrix.incTable(key1, key1, wItems.get(key1) / total);

    		for (int j = i+1; j < itemList.size(); ++j) {
    			Long key2 = itemList.get(j);
    			double avg = (wItems.get(key1) + wItems.get(key2)) / 2.0;
    			
    			matrix.incTable(key1, key2, avg/total);
    			matrix.incTable(key2, key1, avg/total);
    		}
    	}
    }
    
	@Override
	public List<Long> getKeys() {
		return Lists.newArrayList(itemTotals.keySet());
	}   
    
    public void rowNormalize() { 
    	for (Long row : itemTotals.keySet()) { 
    		double k = itemTotals.get(row);
    		for (Long col : itemTotals.keySet()) { 
    			matrix.put(row, col, matrix.get(row, col) / k);
    		}
    	}
    }
    
    public static void main(String[] args) throws Exception {     	
    	HeatS.LOG_RECORDS = 20000;
    	HeatS heats = new HeatS();
    	RewardEmitter emitter = new BinaryEmitter();

		String input = "/data/steam/training-data.gz";
		String output = "/data/steam/heats.csv";
		
    	heats.loadCompressed(input, emitter);
    	heats.rowNormalize();
    	heats.output(output);
//    	heats.saveMatrixPb("/data/steam/heats.pb", false);
    	LOGGER.info("total number of items " + heats.getKeys().size());
    }
}