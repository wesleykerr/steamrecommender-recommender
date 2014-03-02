package com.wesleykerr.steam.summary.domain;

import com.google.common.base.Preconditions;

public class SiteDetails {
	int numVisitors;
	int numProfiles;
	int numRecomms;
	
	private SiteDetails() { 
		
	}
	
	public int getNumVisitors() { 
		return numVisitors;
	}
	
	public int getNumProfiles() { 
		return numProfiles;
	}
	
	public int getNumRecomms() { 
		return numRecomms;
	}
	
	public static class Builder { 
		SiteDetails details;
		
		private Builder() { 
			details = new SiteDetails();
		}

		public Builder withNumVisitors(int numVisitors) { 
			details.numVisitors = numVisitors;
			return this;
		}
		
		public Builder withNumProfiles(int numProfiles) { 
			details.numProfiles = numProfiles;
			return this;
		}
		
		public Builder withNumRecomms(int numRecomms) { 
			details.numRecomms = numRecomms;
			return this;
		}
		
		public SiteDetails build() { 
			Preconditions.checkNotNull(details);
			SiteDetails tmp = details;
			details = null;
			return tmp;
		}
		
		public static Builder create() { 
			return new Builder();
		}
	}
}
