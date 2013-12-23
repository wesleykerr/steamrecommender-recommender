package com.wesleykerr.steam.domain.player;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;


public class Player {
	
    @SerializedName("_id")
	private String id;

    @SerializedName("_rev")
	private String rev;
	
	private List<GameStats> games;

	private Long updateDateTime;
	private Long friendsMillis;
	
	private boolean visible;
	
	public Player() { 
		
	}

	/**
	 * @return the _id
	 */
	public String getId() {
		return id;
	}

    /**
     * @return the _rev
     */
    public String getRev() {
        return rev;
    }
    

    /**
	 * @return the games
	 */
	public List<GameStats> getGames() {
		return games;
	}

	/**
	 * @return the updateDateTime
	 */
	public Long getUpdateDateTime() {
		return updateDateTime;
	}

	/**
	 * @return the friendsMillis
	 */
	public Long getFriendsMillis() { 
        return friendsMillis;
    }

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}


	public static class Builder {
	    
	    private Player player;
	    
	    private Builder() { 
	        player = new Player();
	    }
	    
	    public Builder withPlayer(Player p) {
	        player.id = p.id;
	        player.rev = p.rev;
	        player.games = Lists.newArrayList(p.games);
	        player.updateDateTime = p.updateDateTime;
	        player.friendsMillis = p.friendsMillis;
	        player.visible = p.visible;
	        return this;
	    }
	    
	    public Builder withId(String id) {
	        player.id = id;
	        return this;
	    }
	    
	    public Builder withRev(String rev) { 
	        player.rev = rev;
	        return this;
	    }
	    
	    public Builder withGames(List<GameStats> games) { 
	        player.games = Lists.newArrayList(games);
	        return this;
	    }
	    
	    public Builder isVisible(boolean visible) {
	        player.visible = visible;
	        return this;
	    }
	    
	    public Builder withUpdateDateTime(Long updateDateTime) { 
	        player.updateDateTime = updateDateTime;
	        return this;
	    }
	    
	    public Builder withFriendsMillis(Long friendsMillis) { 
	        player.friendsMillis = friendsMillis;
	        return this;
	    }
	    
	    public Player build() { 
	        Preconditions.checkNotNull(player);
	        
	        Player tmp = player;
	        player = null;
	        return tmp;
	    }
	    
	    public static Builder create() { 
	        return new Builder();
	    }
	}
}
