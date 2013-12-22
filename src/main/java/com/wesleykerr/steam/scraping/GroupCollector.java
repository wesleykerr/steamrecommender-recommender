package com.wesleykerr.steam.scraping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.persistence.dao.CounterDAO;
import com.wesleykerr.steam.persistence.dao.PlayerURLsDAO;
import com.wesleykerr.steam.persistence.dao.SteamPlayerDAO;
import com.wesleykerr.steam.persistence.memory.CounterDAOImpl;
import com.wesleykerr.steam.persistence.nosql.SteamPlayerDAOImpl;
import com.wesleykerr.steam.persistence.sql.PlayerURLsDAOImpl;
import com.wesleykerr.utils.Utils;

public class GroupCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupCollector.class);
    private static final String baseURL_ = "http://steamcommunity.com/";

    private static final int MAX_CALLS = 1000;
    private static final int NUM_PAGES = 100;

    private QueryDocument queryDocument;

    private PlayerURLsDAO playerDAO;
    private SteamPlayerDAO steamPlayerDAO;

    private CounterDAO counterDAO;
    private CounterDAO playerCounterDAO;

    public GroupCollector(PlayerURLsDAO playerDAO, SteamPlayerDAO steamPlayerDAO) { 
        this.playerDAO = playerDAO;
        this.steamPlayerDAO = steamPlayerDAO;

        playerCounterDAO = new CounterDAOImpl();
        
        counterDAO = new CounterDAOImpl();
        queryDocument = new QueryDocument(counterDAO);
    }
    
    public int getNumberPlayersAdded() { 
        return playerCounterDAO.getCounter();
    }

    /**
     * Iterate over the players and make sure to extract their
     * steam ids.
     * @param players
     */
    private void addPlayers(Elements players) {
        Set<String> storedURLs = Sets.newHashSet();
        
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
                long steamId = Long.parseLong(playerURL.substring(index+1));
                boolean added = steamPlayerDAO.add(steamId);
                if (added)
                    playerCounterDAO.incrCounter();
            } else if (playerURL.contains("id")) { 
                if (!storedURLs.contains(playerURL)) {
                    LOGGER.debug("Storing [" + playerURL + "]");
                    playerDAO.add(playerURL);
                    storedURLs.add(playerURL);
                }
            } else {
                LOGGER.error("Error: " + playerURL);
            }
        }
    }

    /**
     * Grab the website and figure out what we are going to do with it.
     * @param query
     */
    public void scrape(String prefix, String game, String query) { 
        String url = baseURL_ + prefix + "/" + game + "/members/";
        LOGGER.debug("URL: " + url);
        Document doc = queryDocument.request(url, 10);

        int maxPage = getMaxPage(doc);
        LOGGER.info("Maximum Number of Pages: " + maxPage);

        processDocument(doc, query);
        if (maxPage < NUM_PAGES) { 
            processAllPages(url, query, maxPage);
        } else {
            processRandomPages(url, query, maxPage);
        }
    }
    
    /**
     * With any remaining calls that we have, we can begin to scrape
     * details about players that are not using their steamids as 
     * their membership id.
     */
    public void processPlayerURLs() { 
        playerCounterDAO.reset();
        int remaining = MAX_CALLS - counterDAO.getCounter();
        if (remaining <= 0)
            return;
        
        LOGGER.info("Pulling details for " + remaining + " players");
        scrapeSteamIds(remaining);
    }
    
    /**
     * Begin processing player urls up to the maximum
     * count.
     * @param count
     */
    public void scrapeSteamIds(int count) { 
        List<String> urls = playerDAO.fetch(count);
        for (String playerURL : urls) { 
            try { 
                String url = playerURL + "/?xml=1";
                
                Document doc = queryDocument.request(url, 10);
                Element idElement = doc.select("steamid64").first();                    

                long steamId = Long.parseLong(idElement.text());
                boolean added = steamPlayerDAO.add(steamId);
                if (added)
                    playerCounterDAO.incrCounter();

                playerDAO.delete(playerURL);
                Utils.delay(1500);
            } catch (Exception e) { 
                LOGGER.error("Error processing " + playerURL);
                LOGGER.error("MSG:", e);
            }
        }
    }
    
    protected void processAllPages(String url, String query, int maxPage) { 
        for (int i = 2; i <= maxPage; ++i) { 
            LOGGER.info(" ***** PAGE " + i);
            LOGGER.debug(url + "?p=" + i);
            Document page = queryDocument.request(url + "?p=" + i, 10);
            processDocument(page, query);
            Utils.delay(3000);
        }
    }
    
    protected void processRandomPages(String url, String query, int maxPage) { 
        Random r = new Random();
        Set<Integer> all = Sets.newTreeSet();
        for (int i = 2; i <= maxPage; ++i)
            all.add(i);
        List<Integer> list = new ArrayList<Integer>(all);
        Collections.shuffle(list, r);
        
        for (int i = 0; i < NUM_PAGES && i < all.size(); ++i) { 
            int page = list.get(i);
            LOGGER.info(" ***** PAGE " + page);
            LOGGER.debug(url + "?p=" + page);
            Document pageDoc = queryDocument.request(url + "?p=" + page, 10);
            processDocument(pageDoc, query);
            Utils.delay(3000);
        }
    }

    protected void processDocument(Document doc, String query) { 
        Element memberList = doc.select("div#memberList").first();
        Elements players = memberList.select(query);
        addPlayers(players);
    }
    
    /**
     * Iterate through all of the page links to find the max value.
     * Some of the page links will not be numbers so that's why we ignore
     * any number format exceptions we receive.
     * @param doc
     * @return
     */
    protected int getMaxPage(Document doc) { 
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
        return maxPage;
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
        CouchbaseClient client = null;
        MySQL mySQL = null;
        try { 
            List<URI> hosts = Arrays.asList(new URI("http://192.168.0.8:8091/pools"));
            client = new CouchbaseClient(hosts, "default", "");
            SteamPlayerDAO steamPlayerDAO = new SteamPlayerDAOImpl(client);
            
            mySQL = MySQL.getDreamhost();
            PlayerURLsDAO playerDAO = new PlayerURLsDAOImpl(mySQL.getConnection());

            GroupCollector collector = new GroupCollector(playerDAO, steamPlayerDAO);

            String line = selectDetails("/data/starter/games");
            String[] tokens = line.split(",");
            LOGGER.info("Selected: " + line);
            collector.scrape(tokens[0], tokens[1], tokens[2]);
            LOGGER.info("Added: " + collector.getNumberPlayersAdded() + " steamids");

            collector.processPlayerURLs();
            LOGGER.info("Added: " + collector.getNumberPlayersAdded() + " steamids from player URLs");

        } finally { 
            if (mySQL != null)
                mySQL.disconnect();
            if (client != null) 
                client.shutdown();
        }
    }
}


// Sample File:
// groups   rps  a.linkFriend
// games    Skyrim   a
// games    Portal2  a