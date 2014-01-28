package com.wesleykerr.steam.domain.player;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.wesleykerr.utils.GsonUtils;

public class FriendsList {

    @SerializedName("_id")
    private String id;
    @SerializedName("_rev")
    private String rev;

    private List<Relationship> friendsList;
    private Long updateDateTime;
    
    public FriendsList() { 
        
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the rev
     */
    public String getRev() {
        return rev;
    }

    /**
     * @return the friendsList
     */
    public List<Relationship> getFriendsList() {
        return friendsList;
    }

    /**
     * @return the updateDateTime
     */
    public long getUpdateDateTime() {
        return updateDateTime;
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
        
        public Builder withId(String id) {
            friendsList.id = id;
            return this;
        }
        
        public Builder withRev(String rev) { 
            friendsList.rev = rev;
            return this;
        }
        
        public Builder withFriends(List<Relationship> friends) { 
            friendsList.friendsList = Lists.newArrayList(friends);
            return this;
        }
        
        public Builder withUpdateDateTime(long updateDateTime) { 
            friendsList.updateDateTime = updateDateTime;
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
