package com.wesleykerr.steam.scraping;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.JsonObject;
import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.persistence.Couchbase;
import com.wesleykerr.steam.persistence.dao.CounterDAO;
import com.wesleykerr.steam.persistence.memory.CounterDAOImpl;
import com.wesleykerr.utils.Utils;

public class SearchCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchCollector.class);
    private static final String baseURL_ = "http://steamcommunity.com/actions/Search?T=Account&";

    private String keywords;

    private QueryDocument queryDocument;
    private CouchbaseClient client;

    private int queryCount;

    public SearchCollector(String keywords) throws Exception { 
        this.keywords = keywords;

        CounterDAO counter = new CounterDAOImpl();
        queryDocument = new QueryDocument(counter);

        client = Couchbase.connect("default");
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
                LOGGER.error("Error: " + playerURL);
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
    
    public void search() { 
        String url = baseURL_ + "/actions/Search?T=Account&K=\"" + keywords + "\"";
        LOGGER.debug("URL: " + url);
        Document doc = queryDocument.request(url, 10);

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
        for (int i = 1; i <= maxPage; ++i) { 
            LOGGER.info(" ***** PAGE " + i);
            Document page = queryDocument.request(url + "&p=" + i, 10);
            Elements players = page.select(".linkTitle");
            addPlayers(players);
            Utils.delay(5000);
        }
    }

    public static void main(String[] args) throws Exception { 
        SearchCollector si = new SearchCollector("dota");
        si.search();
    }
}
