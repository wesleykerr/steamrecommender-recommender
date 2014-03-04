package com.wesleykerr.steam.summary.domain;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class JobDetails {

	List<String> failedJobs;
	
	private JobDetails() { 
		failedJobs = Lists.newArrayList();
	}
	
	public List<String> getFailedJobs() { 
		return failedJobs;
	}
	
	public static class Builder {
		private JobDetails details;
		
		private Builder() { 
			details = new JobDetails();
		}

		public Builder withFailedJob(String jobName) {
			details.failedJobs.add(jobName);
			return this;
		}
		
		public Builder withFailedJobs(List<String> jobs) { 
			details.failedJobs = Lists.newArrayList(jobs);
			return this;
		}

		public JobDetails build() { 
			Preconditions.checkNotNull(details);
			JobDetails tmp = details;
			details = null;
			return tmp;
			
		}
		public static Builder create() { 
			return new Builder();
		}
	}
}
