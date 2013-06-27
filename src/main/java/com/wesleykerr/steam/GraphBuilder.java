package com.wesleykerr.steam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.api.PlayerInfo;
import com.wesleykerr.steam.model.Player;
import com.wesleykerr.steam.persistence.CounterDAO;
import com.wesleykerr.steam.persistence.FriendsDAO;
import com.wesleykerr.steam.persistence.GameStatsDAO;
import com.wesleykerr.steam.persistence.PlayerDAO;
import com.wesleykerr.steam.persistence.mysql.CounterDAOImpl;
import com.wesleykerr.steam.persistence.mysql.FriendsDAOImpl;
import com.wesleykerr.steam.persistence.mysql.GameStatsDAOImpl;
import com.wesleykerr.steam.persistence.mysql.MySQL;
import com.wesleykerr.steam.persistence.mysql.PlayerDAOImpl;

public class GraphBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphBuilder.class);

	public static void main(String[] args) {
		System.setProperty("steam.key", "72A809B286ED454CC53C4D03EF798EE4");
		
		MySQL mySQL = MySQL.getLocalhost();
		
		PlayerDAO playerDAO = new PlayerDAOImpl(mySQL.getConnection());
		CounterDAO counterDAO = new CounterDAOImpl(mySQL.getConnection());
		FriendsDAO friendsDAO = new FriendsDAOImpl(mySQL.getConnection());
		GameStatsDAO gameStatsDAO = new GameStatsDAOImpl(mySQL.getConnection());
		
		QueryDocument query = new QueryDocument(counterDAO);
		PlayerInfo info = new PlayerInfo(query);

		for (Long steamId : playerDAO.getPlayers(1000)) { 
			LOGGER.info("Steam Id: " + steamId);
			Player p = info.gatherPlayerInfo(steamId);
			playerDAO.update(p);
			
//			List<GameStats> list = info.gatherOwnedGames(steamId);
//			// if the profile is private we cannot access their friends list either.
//			if (list.size() == 0)  
//				continue;
//
//			gameStatsDAO.update(list);
//			
//			List<Friend> friends = info.gatherFriends(steamId);
//			
//			for (Friend friend : friends) { 
//				playerDAO.add(friend.getSteamId2(), "Friend [" + steamId + "]");
//				friendsDAO.add(friend);
//			}
			
//			LOGGER.info("Steam Id: " + steamId + " Games: " + list.size() + " Friends: " + friends.size());
		}
	}
}
