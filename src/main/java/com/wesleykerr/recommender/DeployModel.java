package com.wesleykerr.recommender;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.persistence.dao.ItemItemModelDAO;
import com.wesleykerr.steam.persistence.sql.ItemItemModelDAOImpl;

/**
 * This class will take the given item-item model
 * and store it by columns in the given data
 * store.
 * @author wkerr
 *
 */
public class DeployModel {

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
    
    /**
     * Update the model columns in the dao.
     * @param dao
     */
    public void deployColumns(ItemItemModelDAO dao) throws Exception { 
        Joiner joiner = Joiner.on(",");
        dao.setColumn(modelId, -1L, joiner.join(columns));
        for (int col = 0; col < numItems; ++col) { 
            dao.setColumn(modelId, columns.get(col), columnToString(col, joiner));
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
            matrix[col-1][row] = Double.parseDouble(lineTokens[col]);
        }
    }
    
    protected String columnToString(int col, Joiner joiner) { 
        return joiner.join(matrix[col]);
    }
    
    /**
     * Return the columns.
     * @return
     */
    public List<Long> getColumns() { 
        return columns;
    }
    
    public static void main(String[] args) throws Exception { 
        // TODO read these in as a parameters
        int modelId = 1;  
        String input = "/data/steam/model.csv";
        
        DeployModel deployModel = new DeployModel(modelId);
        deployModel.loadMatrix(input);

        MySQL mysql = MySQL.getDreamhost();
        ItemItemModelDAO modelDAO = new ItemItemModelDAOImpl(mysql.getConnection());
        deployModel.deployColumns(modelDAO);
    }
}