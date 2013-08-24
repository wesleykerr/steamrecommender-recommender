package com.wesleykerr.recommender;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.wesleykerr.generated.MatrixProtos.Matrix;
import com.wesleykerr.recommender.utils.RecommMatrix;

public class ItemItemCFTest {

	@Test
	public void testOutputPB() { 
		RecommMatrix matrix = new RecommMatrix();
		matrix.incTable(0, 0, 1);
		matrix.incTable(0, 1, 1);
		matrix.incTable(1, 1, 1);
		matrix.incTable(2, 2, 1);
		
		try { 
			ItemItemCF.saveMatrixPB("/tmp/testoutput", Lists.newArrayList(0L,1L,2L), matrix, false);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
		
		Matrix.Builder builder = Matrix.newBuilder();
		try { 
			builder.mergeFrom(new FileInputStream("/tmp/testoutput"));

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
		
		Matrix m = builder.build();
		assertEquals(3, m.getRowCount());
		assertEquals(3, m.getColCount());
		assertEquals(Lists.newArrayList(1d,1d,0d,0d,1d,0d,0d,0d,1d), m.getDataList());
	}
}
