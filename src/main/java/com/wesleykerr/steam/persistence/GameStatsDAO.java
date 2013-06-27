package com.wesleykerr.steam.persistence;

import java.util.List;

import com.wesleykerr.steam.model.GameStats;

public interface GameStatsDAO {

	void update(GameStats stats);
	void update(List<GameStats> list);
}
