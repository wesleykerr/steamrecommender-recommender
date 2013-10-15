package com.wesleykerr.steam.scraping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.Utils;
import com.wesleykerr.steam.gson.Player;
import com.wesleykerr.steam.persistence.CounterDAO;
import com.wesleykerr.steam.persistence.memory.CounterDAOImpl;

public class GroupCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupCollector.class);
    private static final String baseURL_ = "http://steamcommunity.com/";

    private String prefix;
    private String game;

    private QueryDocument queryDocument;
    private CouchbaseClient client;
    
    private int queryCount;

    public GroupCollector(String prefix, String game) throws Exception { 
        this.prefix = prefix;
        this.game = game;

        CounterDAO counter = new CounterDAOImpl();
        queryDocument = new QueryDocument(counter);

//        List<URI> hosts = Arrays.asList(new URI("http://127.0.0.1:8091/pools"));
        List<URI> hosts = Arrays.asList(new URI("http://192.168.0.8:8091/pools"));
        client = new CouchbaseClient(hosts, "default", "");
        
        queryCount = 0;
    }

    private String gatherSteamId(String playerURL) { 
        try {
            Thread.currentThread().sleep(1500);
        } catch (Exception e) { 
            e.printStackTrace();
        }
        
        String url = playerURL + "/?xml=1";
        Document doc = queryDocument.request(url, 10);
        Element idElement = doc.select("steamid64").first();                    
        
        ++queryCount;
        return idElement.text();
    }

    private void add(String steamStr) { 
        long steamId = Long.parseLong(steamStr);
        Object o = client.get(String.valueOf(steamId));
        if (o == null) {
            LOGGER.info("SteamId: " + steamId + " does not exist");
            JsonObject obj = new JsonObject();
            obj.addProperty("_id", String.valueOf(steamId));
            obj.addProperty("updateDateTime", 0);
            client.add(String.valueOf(steamId), obj.toString());
        }
    }

    /**
     * Iterate over the players and make sure to extract their
     * steam ids.
     * @param players
     */
    private void addPlayers(Elements players) {
        for (Element e : players) { 
            String playerURL = e.attr("href");
            if (playerURL.startsWith("steam:"))
                continue;

            int index = playerURL.lastIndexOf('/');
            if (index == -1) { 
                LOGGER.warn("Unknown URL: " + playerURL);
                continue;
            }

            if (playerURL.contains("profiles")) { 
                add(playerURL.substring(index+1));
            } else if (playerURL.contains("id")) { 
                add(gatherSteamId(playerURL));
            } else {
                LOGGER.error("Error: " + playerURL);
            }
        }
    }

    /**
     * Grab the website and figure out what we are going to do with it.
     * @param query
     */
    public void scrape(String query) { 
        String url = baseURL_ + prefix + "/" + game + "/members/";
        LOGGER.debug("URL: " + url);
        Document doc = queryDocument.request(url, 10);
        ++queryCount;

        int maxPage = -1;
        Elements pageLinks = doc.select("div.pageLinks > a");
        for (Element e : pageLinks) { 
            String text = e.text();
            try { 
                int value = Integer.parseInt(text);
                maxPage = Math.max(maxPage, value);
            } catch (NumberFormatException exp) { 
                // ignore.
            }
        }
        LOGGER.info("Maximum Number of Pages: " + maxPage);
        for (int i = 1; i <= maxPage && i <= 50 && queryCount < 500; ++i) { 
            LOGGER.info(" ***** PAGE " + i);
            LOGGER.debug(url + "?p=" + i);
            Document page = queryDocument.request(url + "?p=" + i, 10);
            ++queryCount;

            Element memberList = page.select("div#memberList").first();
            Elements players = memberList.select(query);
            LOGGER.debug(players.toString());
            addPlayers(players);
            Utils.delay(10000);
        }
    }
    
    private void test(String steamId) throws Exception { 
        Gson gson = new Gson();
        Object o = client.get(String.valueOf(steamId));
        Player p = gson.fromJson((String) o, Player.class);
        LOGGER.info("query player " + p.get_id());
    }

    /**
     * Select a game from our list that we will be pulling
     * steam ids from.  The return value is a string that contains
     * the name of the game and the query that we need to run to be successful.
     * @param file
     * @return
     * @throws Exception
     */
    public static String selectDetails(String file) throws Exception { 
        BufferedReader in = new BufferedReader(new FileReader(file));
        List<String> games = Lists.newArrayList();
        while (in.ready()) { 
            games.add(in.readLine());
        }
        in.close();

        Random random = new Random(System.currentTimeMillis());
        return games.get(random.nextInt(games.size()));
    }

    public static void main(String[] args) throws Exception { 
        // Read from a file one of a list of games.  Select one at random
        // and select n random pages to pull and dump into the Couchbase datastore.
        String line = selectDetails("/data/starter/games");
        LOGGER.info("Selected: " + line);

        String[] tokens = line.split("\t");
        GroupCollector gi = new GroupCollector(tokens[0], tokens[1]);
        gi.scrape(tokens[2]);
        
        // did not stop...
    }
}


// Sample File:
// groups   rps  a.linkFriend
// games    Skyrim   a
// games    Portal2  a