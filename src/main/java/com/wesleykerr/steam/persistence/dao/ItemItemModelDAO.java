package com.wesleykerr.steam.persistence.dao;

public interface ItemItemModelDAO {

    
    /**
     * Insert or update the given column.
     * @param modelId
     * @param columnId
     * @param column
     */
    void setColumn(int modelId, long columnId, String column) throws Exception;

    /**
     * Retrieve the column.
     * @param modelId
     * @param columnId
     * @return
     * @throws Exception
     */
    String getColumn(int modelId, long columnId) throws Exception;
    
    /**
     * Remove all of the associated data for this model
     * @param modelId
     * @throws Exception
     */
    void delete(int modelId) throws Exception;

}
