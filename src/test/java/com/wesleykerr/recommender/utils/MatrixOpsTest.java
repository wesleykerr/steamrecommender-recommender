package com.wesleykerr.recommender.utils;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class MatrixOpsTest {

	@Test
	public void testCosine() { 
	    List<Double> a = Lists.newArrayList(0d,1d,0d);
	    List<Double> b = Lists.newArrayList(1d,1d,1d);
	    assertEquals(new Double(1d/Math.sqrt(3)), MatrixOps.cosine(a,b), 0.00001);
	    
        a = Lists.newArrayList(1d,1d,0d);
        b = Lists.newArrayList(1d,1d,1d);
        assertEquals(new Double(2d/(Math.sqrt(3)*Math.sqrt(2))), MatrixOps.cosine(a,b), 0.00001);
        
        a = Lists.newArrayList(0.5d,0.5d,0d);
        b = Lists.newArrayList(1d,1d,1d);
        assertEquals(new Double(1d/(Math.sqrt(0.5)*Math.sqrt(3))), MatrixOps.cosine(a,b), 0.00001);
        
        a = Lists.newArrayList(0d, 1d);
        b = Lists.newArrayList(1d, 0d);
        assertEquals(new Double(0d), MatrixOps.cosine(a,b), 0.00001);
	}
}
