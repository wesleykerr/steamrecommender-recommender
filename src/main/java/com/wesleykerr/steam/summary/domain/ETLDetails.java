package com.wesleykerr.steam.summary.domain;

import com.google.common.base.Preconditions;

public class ETLDetails {
	int numUpdated;
	int numPulled;
	int numFound;
	
	int numFriends;
	
	int playerCount;
	int privateCount;
	int newPlayersCount;
	
	int sampledPublic;
	int sampledPrivate;
	
	private ETLDetails() { 
		
	}

	public int getNumUpdated() { 
		return numUpdated;
	}
	
	public int getNumPulled() { 
		return numPulled;
	}
	
	public int getNumFound() { 
		return numFound;
	}
	
	public int getNumFriends() { 
		return numFriends;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public int getPrivateCount() {
		return privateCount;
	}

	public int getNewPlayersCount() {
		return newPlayersCount;
	}


	public int getSampledPublic() {
		return sampledPublic;
	}

	public int getSampledPrivate() {
		return sampledPrivate;
	}


	public static class Builder {
		private ETLDetails details;
		
		private Builder() {
			details = new ETLDetails();
		}
		
		public Builder withNumUpdated(int numUpdated) { 
			details.numUpdated = numUpdated;
			return this;
		}
		
		public Builder withNumPulled(int numPulled) { 
			details.numPulled = numPulled;
			return this;
		}
		
		public Builder withNumFound(int numFound) { 
			details.numFound = numFound;
			return this;
		}
		
		public Builder withNumFriends(int numFriends) { 
			details.numFriends = numFriends;
			return this;
		}
		
		public Builder withPlayerCount(int playerCount) { 
			details.playerCount = playerCount;
			return this;
		}
		
		public Builder withPrivateCount(int privateCount) { 
			details.privateCount = privateCount;
			return this;
		}
		
		public Builder withNewPlayersCount(int newPlayersCount) { 
			details.newPlayersCount = newPlayersCount;
			return this;
		}
		
		public Builder withSampledPublic(int sampledPublic) { 
			details.sampledPublic = sampledPublic;
			return this;
		}
		
		public Builder withSampledPrivate(int sampledPrivate) { 
			details.sampledPrivate = sampledPrivate;
			return this;
		}
		
		public ETLDetails build() { 
			Preconditions.checkNotNull(details);
			ETLDetails tmp = details;
			details = null;
			return tmp;
		}
		
		public static Builder create() { 
			return new Builder();
		}
	}
}
