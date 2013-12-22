package com.wesleykerr.recommender.utils;

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class MatrixOps {

    /**
     * Compute cosine between two columns
     * @param a
     * @param b
     * @return
     */
    public static Double cosine(List<Double> a, List<Double> b) { 
        Preconditions.checkState(a.size() == b.size());
        double numerator = 0;
        double lengthA = 0;
        double lengthB = 0;
        for (int i = 0; i < a.size(); ++i) { 
            numerator += a.get(i)*b.get(i);
            lengthA += a.get(i)*a.get(i);
            lengthB += b.get(i)*b.get(i);
        }
        return numerator / (Math.sqrt(lengthA)*Math.sqrt(lengthB));
    }
    
    /**
     * Create a new matrix that contains the cosine distance 
     * between each of the items.
     * @param items
     * @param matrix
     * @return
     */
    public static RecommMatrix cosine(List<Long> items, RecommMatrix matrix) { 
        RecommMatrix result = new RecommMatrix();
        for (Long key1 : items) { 
            for (Long key2 : items) {
                double value = matrix.get(key1, key2);
                double a = matrix.get(key1, key1);
                double b = matrix.get(key2, key2);
                
                double sim = value / (Math.sqrt(a)*Math.sqrt(b));
                result.incTable(key1, key2, sim);
            }
        }
        return result;
    }
    
    public static RecommMatrix rowNormalize(List<Long> items, RecommMatrix matrix) { 
        Map<Long,Double> itemTotals = Maps.newHashMap();
        for (Long key1 : items) { 
            itemTotals.put(key1, 0d);
            for (Long key2 : items) { 
                itemTotals.put(key1, itemTotals.get(key1) + matrix.get(key1, key2));
            }
        }

        RecommMatrix result = new RecommMatrix();
        for (Long key1 : items) { 
            double total = itemTotals.get(key1);
            for (Long key2 : items) { 
                result.incTable(key1, key2, matrix.get(key1, key2) / total);
            }
        }
        return result;
    }
}
