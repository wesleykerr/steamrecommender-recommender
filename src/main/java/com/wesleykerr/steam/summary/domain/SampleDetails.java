package com.wesleykerr.steam.summary.domain;

import com.google.common.base.Preconditions;

public class SampleDetails {

    String date;
    
	int sampledPublic;
	int sampledPrivate;
	
	private SampleDetails() { 
		
	}

	public String getDate() { 
	    return date;
	}

	public int getSampledPublic() {
		return sampledPublic;
	}

	public int getSampledPrivate() {
		return sampledPrivate;
	}


	public static class Builder {
		private SampleDetails details;
		
		private Builder() {
			details = new SampleDetails();
		}
		
		public Builder withDate(String date) {
		    details.date = date;
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
		
		public SampleDetails build() { 
			Preconditions.checkNotNull(details);
			SampleDetails tmp = details;
			details = null;
			return tmp;
		}
		
		public static Builder create() { 
			return new Builder();
		}
	}
}
