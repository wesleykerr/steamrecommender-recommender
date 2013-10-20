package com.wesleykerr.steam.persistence.dao;

import java.util.List;

public interface PlayerURLsDAO {

    public void add(String playerURL);
    public void delete(String playerURL);
    
    public List<String> fetch(int count);
}
