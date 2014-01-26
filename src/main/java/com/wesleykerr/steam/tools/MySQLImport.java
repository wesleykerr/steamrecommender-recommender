package com.wesleykerr.steam.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.persistence.dao.SteamPlayerDAO;
import com.wesleykerr.steam.persistence.sql.SteamPlayerDAOImpl;
import com.wesleykerr.utils.GsonUtils;

public class MySQLImport {

    
    public static void main(String[] args) throws Exception { 
        File inputFile = new File("/tmp/players.gz");
        MySQL mySQL = MySQL.getDreamhost();
        SteamPlayerDAO playerDAO = new SteamPlayerDAOImpl(mySQL.getConnection());
        
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(inputFile));
                Reader reader = new InputStreamReader(gzipInputStream);
                BufferedReader input = new BufferedReader(reader)) { 

            for (int i = 0; input.ready() && i < 100; ++i) { 
                String json = input.readLine();
                Player p = GsonUtils.getDefaultGson().fromJson(json, Player.class);

                long steamId = Long.parseLong(p.getId());
                playerDAO.update(steamId, 1, !p.isVisible(), p.getUpdateDateTime(), json);
            }
        }
        
        playerDAO.close();
        mySQL.disconnect();
    }
}
