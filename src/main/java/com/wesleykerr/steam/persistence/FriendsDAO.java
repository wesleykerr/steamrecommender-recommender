package com.wesleykerr.steam.persistence;

import java.util.List;

import com.wesleykerr.steam.model.Friend;

public interface FriendsDAO {

	void add(Friend friend);
	void add(List<Friend> friends);
}
