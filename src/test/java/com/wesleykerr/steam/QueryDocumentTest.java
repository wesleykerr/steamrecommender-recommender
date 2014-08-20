package com.wesleykerr.steam;

import static org.junit.Assert.*;

import org.junit.Test;

import com.wesleykerr.steam.persistence.dao.CounterDAO;
import com.wesleykerr.steam.persistence.memory.CounterDAOImpl;

public class QueryDocumentTest {

    @Test
    public void testIfExists() { 
        CounterDAO counter = new CounterDAOImpl();
        QueryDocument queryDoc = new QueryDocument(counter);
        
        assertTrue(queryDoc.checkIfExists("http://www.wesley-kerr.com", "wesley-kerr.com", 2));
        assertTrue(queryDoc.checkIfExists("http://www.steamrecommender.com/img/applogo.gif", "steamrecommender.com", 2));
        
        assertFalse(queryDoc.checkIfExists("http://www.wesley-kerr.com/missing.html", "wesley-kerr.com", 2));
    }
}
