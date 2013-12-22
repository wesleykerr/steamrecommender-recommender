package com.wesleykerr.steam.domain.player;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wesleykerr.utils.GsonUtils;

public class FriendsListTest {

    @Test 
    public void testSerialization() { 
        JsonObject obj = new JsonObject();
        obj.addProperty("_id", "1234");
        obj.addProperty("_rev", "1");

        JsonArray array = new JsonArray();
        JsonObject obj1 = new JsonObject();
        obj1.addProperty("steamid", "456");
        obj1.addProperty("relationship", "friend");
        obj1.addProperty("friend_since", 0L);
        array.add(obj1);
        
        JsonObject obj2 = new JsonObject();
        obj2.addProperty("steamid", "789");
        obj2.addProperty("relationship", "friend");
        obj2.addProperty("friend_since", 0L);
        array.add(obj2);
        
        obj.add("friendsList", array);
        obj.addProperty("updateDateTime", 0L);
        
        System.out.println(obj.toString());
        
        FriendsList list = GsonUtils.getDefaultGson().fromJson(obj.toString(), FriendsList.class);
        System.out.println(GsonUtils.getDefaultGson().toJson(list));
        
        assertEquals(obj.toString(), GsonUtils.getDefaultGson().toJson(list));
    }
}
