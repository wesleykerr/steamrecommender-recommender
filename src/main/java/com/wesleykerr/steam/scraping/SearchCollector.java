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

public class SearchCollector {
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchCollector.class);
	private static final String baseURL_ = "http://steamcommunity.com/actions/Search?T=Account&";
	
	private String keywords;
	
	private PlayerDAO playerDAO;
	private QueryDocument queryDocument;
	
	public SearchCollector(String keywords) { 
		this.keywords = keywords;
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
			int index = playerURL.lastIndexOf('/');
			if (index == -1) { 
				LOGGER.error("Error: " + playerURL);
				continue;
			}
			
			if (playerURL.contains("profiles")) { 
				long value = Long.parseLong(playerURL.substring(index+1));
				playerDAO.add(value, "SearchCollector: " + keywords);
				
			} else if (playerURL.contains("id")) { 
				long value = gatherSteamId(playerURL);
				playerDAO.add(value, "SearchCollector: " + keywords);
				
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
		MySQL mySQL = new MySQL();
		mySQL.setHost("localhost");
		mySQL.setPort(3306);
		mySQL.setDb("game_recommender");
		mySQL.setUsername("root");
		mySQL.connect();
		
		PlayerDAO playerDAO = new PlayerDAOImpl(mySQL.getConnection());
		CounterDAO counterDAO = new CounterDAOImpl(mySQL.getConnection());
		
		QueryDocument queryDocument = new QueryDocument(counterDAO);
		
		SearchCollector si = new SearchCollector("dota");
		si.setPlayerDAO(playerDAO);
		si.setQueryDocument(queryDocument);
		si.search();
		
		mySQL.disconnect();
	}
}
