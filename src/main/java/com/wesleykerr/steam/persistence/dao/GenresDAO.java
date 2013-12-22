package com.wesleykerr.steam.persistence.dao;

import java.util.List;
import java.util.Map;

public interface GenresDAO {

    public Map<Long,List<String>> getGenresByAppId() throws Exception;
}
