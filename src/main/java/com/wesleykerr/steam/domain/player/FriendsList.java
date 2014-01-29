package com.wesleykerr.steam.domain.player;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.wesleykerr.utils.GsonUtils;

public class FriendsList {

    private Long steamId;
    private Integer revision;

    private List<Relationship> friendsList;
    private Integer numFriends;
    
    private Long lastUpdated;
    
    public FriendsList() { 
        
    }

    /**
     * @return the steamId
     */
    public Long getSteamId() {
        return steamId;
    }

    /**
     * @return the rev
     */
    public Integer getRevision() {
        return revision;
    }

    /**
     * @return the friendsList
     */
    public List<Relationship> getFriendsList() {
        return friendsList;
    }

    /**
     * @return the numFriends
     */
    public Integer getNumFriends() { 
        return numFriends;
    }
    
    /**
     * @return the updateDateTime
     */
    public Long getLastUpdated() {
        return lastUpdated;
    }

    public static class Relationship {

        private String steamid;
        private String relationship;
        
        @SerializedName("friend_since") 
        private Long friendSince;
        
        public Relationship() { 
            
        }

        /**
         * @return the steamid
         */
        public String getSteamid() {
            return steamid;
        }

        /**
         * @return the relationship
         */
        public String getRelationship() {
            return relationship;
        }

        /**
         * @return the friendSince
         */
        public Long getFriendSince() {
            return friendSince;
        }
        
        @Override
        public String toString() { 
            return GsonUtils.getDefaultGson().toJson(this);
        }
        
    }
    
    public static class Builder { 
        private FriendsList friendsList;
        
        private Builder() { 
            friendsList = new FriendsList();
        }
        
        public Builder withSteamId(Long steamId) {
            friendsList.steamId = steamId;
            return this;
        }
        
        public Builder withRevision(Integer revision) { 
            friendsList.revision = revision;
            return this;
        }
        
        public Builder withFriends(List<Relationship> friends) { 
            if (friends != null) {
                friendsList.friendsList = Lists.newArrayList(friends);
                friendsList.numFriends = friends.size();
            }
            return this;
        }
        
        public Builder withLastUpdated(Long lastUpdated) { 
            friendsList.lastUpdated = lastUpdated;
            return this;
        }

        public FriendsList build() { 
            Preconditions.checkNotNull(friendsList);
            
            FriendsList tmp = friendsList;
            friendsList = null;
            return tmp;
        }
        
        public static Builder create() { 
            return new Builder();
        }
    }

}
