package com.wesleykerr.recommender.utils;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

public class RecommMatrix {
	protected Table<Long,Long,Double> matrix;

    public RecommMatrix() {
    	matrix = TreeBasedTable.create();
    }
    
    /**
     * Increment the value in the table by a given amount.
     * @param row
     * @param col
     * @param v
     */
    public void incTable(long row, long col, double v) {
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
     * Sets the value in the matrix rather than incrementing it.
     * @param row
     * @param col
     * @param v
     */
    public void put(long row, long col, double v) { 
    	matrix.put(row, col, v);
    }
}
