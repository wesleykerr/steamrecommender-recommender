package com.wesleykerr.steam.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.domain.player.Player.Builder;
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
                
                int revision = 1;
                try { 
                    revision = Integer.parseInt(p.getRev());
                } catch (NumberFormatException nfe) { 
                    // do nothing;
                }
                
                boolean isPrivate = !p.isVisible();
                if (p.getGames() != null && p.getGames().size() > 0) {
                    p = Builder.create().withPlayer(p).isVisible(true).build();
                    json = GsonUtils.getDefaultGson().toJson(p);
                    isPrivate = false;                
                }
                
                if (p.getUpdateDateTime() != null && p.getUpdateDateTime() == 0L) { 
                    p = Builder.create().withPlayer(p).withUpdateDateTime(null).build();
                    json = GsonUtils.getDefaultGson().toJson(p);
                }
                
                if (p.getUpdateDateTime() == null && (p.getGames() == null || p.getGames().isEmpty())) {
                    json = null;
                    revision = 0;
                }
                
                System.out.println(" " + json);
                long steamId = Long.parseLong(p.getId());
                playerDAO.addSteamId(steamId);
                playerDAO.update(steamId, revision, isPrivate, p.getUpdateDateTime(), json);
                
                if (true) break;
            }
        }
        
        playerDAO.close();
        mySQL.disconnect();
    }
}
