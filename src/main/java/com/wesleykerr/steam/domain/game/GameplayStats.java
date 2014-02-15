package com.wesleykerr.steam.domain.game;

import com.google.common.base.Preconditions;


public class GameplayStats {
	private Double totalPlaytime;
	private Long owned;
	private Long notPlayed;

	private Double q25Playtime;
	private Double medianPlaytime;
	private Double q75Playtime;

	private GameplayStats() { 
		totalPlaytime = 0.0;
		owned = 0L;
		notPlayed = 0L;
		
		q25Playtime = 0.0;
		medianPlaytime = 0.0;
		q75Playtime = 0.0;
	}

	public Double getTotalPlaytime() {
		return totalPlaytime;
	}


	public Long getOwned() {
		return owned;
	}


	public Long getNotPlayed() {
		return notPlayed;
	}


	public Double getQ25Playtime() {
		return q25Playtime;
	}


	public Double getMedianPlaytime() {
		return medianPlaytime;
	}


	public Double getQ75Playtime() {
		return q75Playtime;
	}


	public static class Builder {
	    private GameplayStats stats;

	    private Builder() { 
	        stats = new GameplayStats();
	    }
	    
	    public void incrementPlaytime(double playtime) { 
	    	stats.totalPlaytime += playtime;
	    }
	    
		public void incrementOwned() { 
			stats.owned += 1L;
		}
		
		public void incrementNotPlayed() { 
			stats.notPlayed += 1L;
		}
		
		public Builder withQ25Playtime(Double playtime) { 
			stats.q25Playtime = playtime;
			return this;
		}

		public Builder withMedianPlaytime(Double playtime) { 
			stats.medianPlaytime = playtime;
			return this;
		}

		public Builder withQ75Playtime(Double playtime) { 
			stats.q75Playtime = playtime;
			return this;
		}

		public GameplayStats build() { 
	        Preconditions.checkNotNull(stats);
	        
	        GameplayStats tmp = stats;
	        stats = null;
	        return tmp;
	    }
	    
	    public static Builder create() { 
	        return new Builder();
	    }
	}
}
