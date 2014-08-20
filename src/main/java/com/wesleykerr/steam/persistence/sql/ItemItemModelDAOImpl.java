package com.wesleykerr.steam.persistence.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.persistence.dao.ItemItemModelDAO;

public class ItemItemModelDAOImpl implements ItemItemModelDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemItemModelDAOImpl.class);

    private Connection conn;

    private PreparedStatement getPS;
    
    private PreparedStatement insertPS;
    private PreparedStatement updatePS;
    private PreparedStatement deletePS;
    
    public ItemItemModelDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public String getColumn(int modelId, long columnId) throws Exception {
        if (getPS == null) 
            getPS = conn.prepareStatement(GET);
        
        getPS.setInt(1, modelId);
        getPS.setLong(2, columnId);
        try (ResultSet rs = getPS.executeQuery()) {
            if (rs.next()) {
                return rs.getString(1);
            }
        }
        return null;
    }
    
    @Override
    public void setColumn(int modelId, long columnId, String column) throws Exception {
        if (insertPS == null || updatePS == null)
            prepareStatements();
        
        int count = update(modelId, columnId, column);
        if (count == 0)
            insert(modelId, columnId, column);
    }
    
    private void prepareStatements() throws Exception { 
        insertPS = conn.prepareStatement(INSERT);
        updatePS = conn.prepareStatement(UPDATE);
        deletePS = conn.prepareStatement(DELETE);
    }

    private void insert(int modelId, long columnId, String column) throws Exception { 
        LOGGER.info("INSERT: modelId: " + modelId + " columnId: " + columnId);
        insertPS.setInt(1, modelId);
        insertPS.setLong(2, columnId);
        insertPS.setString(3, column);
        insertPS.executeUpdate();
    }
    
    private int update(int modelId, long columnId, String column) throws Exception { 
        updatePS.setString(1, column);
        updatePS.setInt(2, modelId);
        updatePS.setLong(3, columnId);
        return updatePS.executeUpdate();
    }
    
    public void delete(int modelId) throws Exception {
        if (deletePS == null) 
            deletePS = conn.prepareStatement(DELETE);

        deletePS.setInt(1, modelId);
        deletePS.executeUpdate();
    }
    
    public static final String INSERT = 
            "insert into models (model_id, appid, model_column) values (?, ?, ?)";
    public static final String UPDATE = 
            "update models set model_column = ? where model_id = ? and appid =  ?";
    public static final String GET = 
            "select model_column from models where model_id = ? and appid = ?";
    
    public static final String DELETE = 
            "delete from models where model_id = ?";
}
