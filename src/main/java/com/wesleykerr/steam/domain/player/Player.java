package com.wesleykerr.steam.domain.player;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


public class Player {
	
	private Long steamId;
	private Integer revision;
	
	private List<GameStats> games;
	private Integer numGames;

	private Long lastUpdated;
	private Long lastUpdatedFriends;
	
	private Boolean isPrivate;
	
	public Player() { 
		
	}

	/**
	 * @return the steamId
	 */
	public Long getSteamId() {
		return steamId;
	}

    /**
     * @return the revision
     */
    public Integer getRevision() {
        return revision;
    }
    

    /**
	 * @return the games
	 */
	public List<GameStats> getGames() {
		return games;
	}
	
	/**
	 * return the number of games that this account
	 * owns.
	 * @return
	 */
	public Integer getNumGames() { 
	    return numGames;
	}

	/**
	 * @return the lastUpdated
	 */
	public Long getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * @return the lastUpdatedFriends
	 */
	public Long getLastUpdatedFriends() { 
        return lastUpdatedFriends;
    }

	/**
	 * @return the visible
	 */
	public Boolean isPrivate() {
		return isPrivate;
	}


	public static class Builder {
	    
	    private Player player;
	    
	    private Builder() { 
	        player = new Player();
	    }
	    
	    public Builder withPlayer(Player p) {
	        player.steamId = p.steamId;
	        player.revision = p.revision;
	        if (p.games != null) {
	            player.games = Lists.newArrayList(p.games);
	            player.numGames = player.games.size();
	        } else { 
	        	player.numGames = 0;
	        }
	        player.lastUpdated = p.lastUpdated;
	        player.lastUpdatedFriends = p.lastUpdatedFriends;
	        player.isPrivate = p.isPrivate;
	        return this;
	    }
	    
	    public Builder withSteamId(Long steamId) {
	        player.steamId = steamId;
	        return this;
	    }
	    
	    public Builder withRevision(Integer revision) { 
	        player.revision = revision;
	        return this;
	    }
	    
	    public Builder withGames(List<GameStats> games) { 
	        if (games != null) {
	            player.games = Lists.newArrayList(games);
	            player.numGames = games.size();
	        }
	        return this;
	    }
	    
	    public Builder withNumGames(Integer numGames) { 
	        player.numGames = numGames;
	        return this;
	    }
	    
	    public Builder isPrivate(Boolean isPrivate) {
	        player.isPrivate = isPrivate;
	        return this;
	    }
	    
	    public Builder withLastUpdated(Long lastUpdated) { 
	        player.lastUpdated = lastUpdated;
	        return this;
	    }
	    
	    public Builder withLastUpdatedFriends(Long lastUpdatedFriends) { 
	        player.lastUpdatedFriends = lastUpdatedFriends;
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
