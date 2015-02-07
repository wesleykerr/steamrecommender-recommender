package com.wesleykerr.steam.persistence.memory;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.wesleykerr.steam.persistence.dao.ItemItemModelDAO;

public class ItemItemModelDAOImpl implements ItemItemModelDAO {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ItemItemModelDAOImpl.class);

    private Map<Integer, Map<Long, String>> map;

    /**
	 */
    public ItemItemModelDAOImpl() {
        this.map = Maps.newHashMap();
    }

    @Override
    public String getColumn(int modelId, long columnId) throws Exception {
        Map<Long, String> appMap = map.get(modelId);
        if (appMap == null)
            return null;
        return appMap.get(columnId);
    }

    @Override
    public void setColumn(int modelId, long columnId, String column)
            throws Exception {
        Map<Long, String> modelMap = map.get(modelId);
        if (modelMap == null) {
            modelMap = Maps.newHashMap();
            map.put(modelId, modelMap);
        }
        modelMap.put(columnId, column);
    }

    public String getColumns(int modelId, long columnId) {
        return map.get(modelId).get(columnId);
    }

    public void delete(int modelId) throws Exception {
        map.remove(modelId);
    }

    @Override
    public int getNextModelId() throws Exception {
        return -1;
    }

    @Override
    public void switchModels(int modelId) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public int getActiveModelId() throws Exception {
        return -1;
    }
}
