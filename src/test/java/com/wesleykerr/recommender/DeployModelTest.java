package com.wesleykerr.recommender;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.wesleykerr.steam.persistence.memory.ItemItemModelDAOImpl;

public class DeployModelTest {

    DeployModel deployModel;
    
    @Before
    public void initialize() { 
        deployModel = new DeployModel(-1);
    }
    
    @Test
    public void testParseColumns() { 
        String testString = "item,1,2,3";
        int count = deployModel.parseColumns(testString);
        assertEquals(3, count);
        
        assertEquals(deployModel.getColumns(), Lists.newArrayList(1L, 2L, 3L));
    }
    
    @Test
    public void testColumnToString() { 
        String header = "item,1,2,3";
        int count = deployModel.parseColumns(header);
        deployModel.initializeMatrix(count);
        deployModel.updateRow(0, "1,1,0,0");
        deployModel.updateRow(1, "2,1,1,0");
        deployModel.updateRow(2, "3,1,0,1");
        
        Joiner joiner = Joiner.on(",");
        assertEquals("1.0,1.0,1.0", deployModel.columnToString(0, joiner));
        assertEquals("0.0,1.0,0.0", deployModel.columnToString(1, joiner));
        assertEquals("0.0,0.0,1.0", deployModel.columnToString(2, joiner));
    }
    
    @Test
    public void testAll() { 
        try {
            File f = File.createTempFile("abc", ".csv");
            BufferedWriter out = new BufferedWriter(new FileWriter(f));
            out.write("item,1,2,3\n");
            out.write("1,1,0,0\n");
            out.write("2,1,1,0\n");
            out.write("3,1,0,1\n");
            out.close();
            deployModel.loadMatrix(f.toString());

            ItemItemModelDAOImpl modelDAO = new ItemItemModelDAOImpl();
            deployModel.deployColumns(modelDAO);

            assertEquals("1.0,1.0,1.0", modelDAO.getColumns(-1, 1));
            assertEquals("0.0,1.0,0.0", modelDAO.getColumns(-1, 2));
            assertEquals("0.0,0.0,1.0", modelDAO.getColumns(-1, 3));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

/**
private int modelId;

private List<Long> columns;
private Double[][] matrix;

private int numItems;

public DeployModel(int modelId) { 
    this.modelId = modelId;
}

public void loadMatrix(String file) throws Exception { 
    BufferedReader in = new BufferedReader(new FileReader(file));
    int results = parseColumns(in.readLine());
    
    initializeMatrix(results);
    int row = 0;
    while (in.ready()) { 
        updateRow(row, in.readLine());
        ++row;
    }
    in.close();
}

public void deployColumns(ItemItemModelDAO dao) throws Exception { 
    Joiner joiner = Joiner.on(",");
    for (int col = 0; col < numItems; ++col) { 
        dao.setColumn(modelId, col, columnToString(col, joiner));
    }
}

protected int parseColumns(String s) { 
    columns = Lists.newArrayList();
    String[] headerTokens = s.split(",");
    for (int i = 1; i < headerTokens.length; ++i)  
        columns.add(Long.parseLong(headerTokens[i]));
    return columns.size();
}

protected void initializeMatrix(int numItems) { 
    this.numItems = numItems;
    matrix = new Double[numItems][numItems];
}

protected void updateRow(int row, String s) { 
    String[] lineTokens = s.split(",");
    for (int col = 1; col < lineTokens.length; ++col) { 
        matrix[col][row] = Double.parseDouble(lineTokens[col]);
    }
}

protected String columnToString(int col, Joiner joiner) { 
    return joiner.join(matrix[col]);
}
*/