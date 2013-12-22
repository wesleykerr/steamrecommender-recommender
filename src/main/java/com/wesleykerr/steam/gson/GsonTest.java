package com.wesleykerr.steam.gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.wesleykerr.steam.domain.player.GameStats;
import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.domain.player.Player.Builder;

public class GsonTest {

	public static void main(String[] args) { 
		
		GameStats g1 = new GameStats();
		g1.setAppid(1);
		g1.setRecentPlaytime(20);
		g1.setCompletePlaytime(100);
		
		GameStats g2 = new GameStats();
		g2.setAppid(2);
		g2.setRecentPlaytime(10);
		g2.setCompletePlaytime(200);
		
		List<GameStats> list = new ArrayList<GameStats>();
		list.add(g1);
		list.add(g2);

		GregorianCalendar defaultCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        defaultCal.set(Calendar.YEAR, 2000);
        defaultCal.set(Calendar.MONTH, 1);
        defaultCal.set(Calendar.DAY_OF_MONTH, 1);
        defaultCal.set(Calendar.HOUR_OF_DAY, 12);
        defaultCal.set(Calendar.MINUTE, 0);
        defaultCal.set(Calendar.SECOND, 0);
		
		Builder builder = Builder.create()
		        .withId("1000")
		        .withGames(list)
		        .withUpdateDateTime(defaultCal.getTime().getTime());
		
		Gson gson = new Gson();
		String json = gson.toJson(builder.build());
		
		System.out.println(json);
		
		Player op = gson.fromJson(json, Player.class);
		System.out.println(op.getId());
		System.out.println(op.getGames());
	}
}
