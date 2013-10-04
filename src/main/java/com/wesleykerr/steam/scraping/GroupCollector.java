package com.wesleykerr.steam.scraping;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.QueryDocument;
import com.wesleykerr.steam.Utils;
import com.wesleykerr.steam.persistence.CounterDAO;
import com.wesleykerr.steam.persistence.PlayerDAO;
import com.wesleykerr.steam.persistence.mysql.CounterDAOImpl;
import com.wesleykerr.steam.persistence.mysql.MySQL;
import com.wesleykerr.steam.persistence.mysql.PlayerDAOImpl;

public class GroupCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupCollector.class);
    private static final String baseURL_ = "http://steamcommunity.com/";

    private String prefix;
    private String game;

    private PlayerDAO playerDAO;
    private QueryDocument queryDocument;

    public GroupCollector(String prefix, String game) { 
        this.prefix = prefix;
        this.game = game;
    }

    public void setPlayerDAO(PlayerDAO playerDAO) { 
        this.playerDAO = playerDAO;
    }

    public void setQueryDocument(QueryDocument queryDocument) { 
        this.queryDocument = queryDocument;
    }

    private long gatherSteamId(String playerURL) { 
        String url = playerURL + "/?xml=1";
        Document doc = queryDocument.request(url, 10);
        Element idElement = doc.select("steamid64").first();                    
        return Long.parseLong(idElement.text());
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
                long value = Long.parseLong(playerURL.substring(index+1));
                boolean added = playerDAO.add(value, "GroupCollector: " + game);
                if (added)
                    LOGGER.debug("Adding[profiles]: " + value);

            } else if (playerURL.contains("id")) { 
                long value = gatherSteamId(playerURL);
                boolean added = playerDAO.add(value, "GroupCollector: " + game);
                if (added)
                    LOGGER.debug("Adding[id]: " + value);

            } else {
                LOGGER.error("Error: " + playerURL);
            }
        }
    }


    public void scrape(String query) { 
        String url = baseURL_ + prefix + "/" + game + "/members/";
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
            LOGGER.debug(url + "?p=" + i);
            Document page = queryDocument.request(url + "?p=" + i, 10);
            Element memberList = page.select("div#memberList").first();
            Elements players = memberList.select(query);
            LOGGER.debug(players.toString());
            addPlayers(players);
            Utils.delay(5000);
        }
    }

    public static void main(String[] args) throws Exception { 
        MySQL mySQL = new MySQL();
        mySQL.setHost("localhost");
        mySQL.setPort(3306);
        mySQL.setDb("game_recommender");
        mySQL.setUsername("root");
        mySQL.connect();

        PlayerDAO playerDAO = new PlayerDAOImpl(mySQL.getConnection());
        CounterDAO counterDAO = new CounterDAOImpl(mySQL.getConnection());

        QueryDocument query = new QueryDocument(counterDAO);

        // pulled RPS
        //              GroupCollector si = new GroupCollector("groups", "rps");
        //              si.scrape("a.linkFriend");
        //              GroupCollector si = new GroupCollector("games", "Skyrim");
        GroupCollector si = new GroupCollector("games", "Portal2");
        si.setPlayerDAO(playerDAO);
        si.setQueryDocument(query);
        si.scrape("a");

        mySQL.disconnect();
    }
}
