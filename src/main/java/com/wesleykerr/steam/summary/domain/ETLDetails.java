package com.wesleykerr.steam.summary.domain;

import com.google.common.base.Preconditions;

public class ETLDetails {
	int numUpdated;
	int numPulled;
	int numFound;
	
	int numFriends;
	
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
