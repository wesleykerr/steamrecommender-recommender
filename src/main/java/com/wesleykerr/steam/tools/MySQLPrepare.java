package com.wesleykerr.steam.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.wesleykerr.steam.domain.player.FriendsList;
import com.wesleykerr.steam.domain.player.FriendsList.Relationship;
import com.wesleykerr.steam.domain.player.GameStats;
import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.utils.GsonUtils;

public class MySQLPrepare {
    private static final Logger LOGGER = Logger.getLogger(MySQLPrepare.class);
    
    private Map<String,Integer> map;

    public String formatLine(String line) { 
        PlayerDeprecated p = GsonUtils.getDefaultGson().fromJson(line, PlayerDeprecated.class);
        Player.Builder builder = Player.Builder.create();
        builder.withSteamId(Long.parseLong(p.getId()));
        if (p.getRev() != null)
            builder.withRevision(Integer.parseInt(p.getRev()));
        else 
            builder.withRevision(1);
        
        if (p.getGames() != null) { 
            List<GameStats> games = Lists.newArrayList(p.getGames());
            for (GameStats game : games) 
                game.setGenres(null);
            builder.withGames(games);
        }
        builder.withLastUpdated(p.getUpdateDateTime());
        builder.withLastUpdatedFriends(p.getFriendsMillis());
        builder.isPrivate(false);
    
        if (p.getUpdateDateTime() == 0L) {
            builder.withLastUpdated(null).isPrivate(null).withRevision(0);
            map.put("zero-date", map.get("zero-date")+1);
        }
            
        if (p.getGames() != null && p.getGames().size() > 0) { 
            builder.isPrivate(false);
            map.put("not-private", map.get("not-private")+1);
        }
        
        if (p.getUpdateDateTime() == 0L && p.getGames() != null && !p.getGames().isEmpty()) {
            builder.withLastUpdated(System.currentTimeMillis());
            map.put("magic-games", map.get("magic-games")+1);
        }
        
        Player newPlayer = builder.build();
        DateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        StringBuffer buf = new StringBuffer();
        buf.append(newPlayer.getSteamId()).append(",");  
        buf.append(newPlayer.getRevision()).append(",");
        buf.append(Objects.firstNonNull(newPlayer.getNumGames(), "NULL")).append(",");
        buf.append(Objects.firstNonNull(newPlayer.isPrivate(), "NULL")).append(",");

        String lastUpdated = "NULL";
        if (newPlayer.getLastUpdated() != null) 
            lastUpdated = formatter.format(new Date(newPlayer.getLastUpdated()));
        buf.append(lastUpdated).append(",");

        String lastUpdatedFriends = "NULL";
        if (newPlayer.getLastUpdatedFriends() != null)
            lastUpdatedFriends = formatter.format(new Date(newPlayer.getLastUpdatedFriends()));
        buf.append(lastUpdatedFriends).append(",");
        buf.append("\'").append(GsonUtils.getDefaultGson().toJson(newPlayer)).append("\'");
        buf.append("\n");
        return buf.toString();
    }
    
    public void preparePlayers() throws Exception { 
        map = Maps.newTreeMap();
        map.put("zero-date", 0);
        map.put("not-private", 0);
        map.put("magic-games", 0);

        int records = 0;
        File inputFile = new File("/tmp/players.gz");
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(inputFile));
                Reader reader = new InputStreamReader(gzipInputStream);
                BufferedReader input = new BufferedReader(reader);
                BufferedWriter output = new BufferedWriter(new FileWriter("/tmp/mysql-players.csv"))) { 

            while (input.ready()) {
                String formatStr = formatLine(input.readLine());
                output.write(formatStr);
                
                ++records;
            }
        }
        
        LOGGER.info("Finished inserting " + records + " records");
        LOGGER.info("...Zero Date: " + map.get("zero-date"));
        LOGGER.info("...Not Private: " + map.get("not-private"));
        LOGGER.info("...Magic Games: " + map.get("magic-games"));
    }
    
    public void prepareFriends() throws Exception { 
        File inputFile = new File("/tmp/friends.gz");
        DateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(inputFile));
                Reader reader = new InputStreamReader(gzipInputStream);
                BufferedReader input = new BufferedReader(reader);
                BufferedWriter output = new BufferedWriter(new FileWriter("/tmp/mysql-friends.csv"))) { 

            while (input.ready()) { 
                String line = input.readLine();
                FriendsListDeprecated list = GsonUtils.getDefaultGson().fromJson(line, FriendsListDeprecated.class);
                FriendsList.Builder builder = FriendsList.Builder.create();
                builder.withSteamId(Long.parseLong(list.getId()));
                builder.withFriends(list.getFriendsList());
                builder.withLastUpdated(list.getUpdateDateTime());
                if (list.getRev() != null) 
                    builder.withRevision(Integer.parseInt(list.getRev()));
                else 
                    builder.withRevision(1);
                FriendsList fl = builder.build();

                StringBuffer buf = new StringBuffer();
                buf.append(fl.getSteamId()).append(",");
                buf.append(fl.getRevision()).append(",");
                buf.append(Objects.firstNonNull(fl.getNumFriends(), "NULL")).append(",");

                Date date = new Date(fl.getLastUpdated());
                buf.append(formatter.format(date)).append(",");

                buf.append("\'").append(GsonUtils.getDefaultGson().toJson(fl)).append("\'").append("\n");
                output.write(buf.toString());
            }
        }
    }

    public static void main(String[] args) throws Exception { 
        MySQLPrepare prepare = new MySQLPrepare();
//        prepare.preparePlayers();
        prepare.prepareFriends();
    }
    
    /**
     * This class is what is stored in the old version of
     * the Couchbase server.  I want to deprecate it now.
     * @author wkerr
     *
     */
    class PlayerDeprecated {
        
        @SerializedName("_id")
        private String id;

        @SerializedName("_rev")
        private String rev;
        
        private List<GameStats> games;

        private Long updateDateTime;
        private Long friendsMillis;
        
        private Boolean visible;
        
        public PlayerDeprecated() { 
            
        }

        /**
         * @return the _id
         */
        public String getId() {
            return id;
        }

        /**
         * @return the _rev
         */
        public String getRev() {
            return rev;
        }
        

        /**
         * @return the games
         */
        public List<GameStats> getGames() {
            return games;
        }

        /**
         * @return the updateDateTime
         */
        public Long getUpdateDateTime() {
            return updateDateTime;
        }

        /**
         * @return the friendsMillis
         */
        public Long getFriendsMillis() { 
            return friendsMillis;
        }

        /**
         * @return the visible
         */
        public boolean isVisible() {
            return visible;
        }
    }
    
    class FriendsListDeprecated {

        @SerializedName("_id")
        private String id;
        @SerializedName("_rev")
        private String rev;

        private List<Relationship> friendsList;
        private Long updateDateTime;
        
        public FriendsListDeprecated() { 
            
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @return the rev
         */
        public String getRev() {
            return rev;
        }

        /**
         * @return the friendsList
         */
        public List<Relationship> getFriendsList() {
            return friendsList;
        }

        /**
         * @return the updateDateTime
         */
        public long getUpdateDateTime() {
            return updateDateTime;
        }
    }
}
