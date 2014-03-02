package com.wesleykerr.steam.summary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.common.collect.Lists;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.summary.domain.ETLDetails;
import com.wesleykerr.steam.summary.domain.JobDetails;
import com.wesleykerr.steam.summary.domain.SiteDetails;

public class SummaryEmail {


	private int getRowCount(Statement s, String tableName, String date) throws Exception {
		String query = "select count(0) from " + tableName + 
				" where create_datetime >= '" + date + "';";
		try (ResultSet rs = s.executeQuery(query)) { 
			if (rs.next()) { 
				return rs.getInt(1);
			}
		}
		return 0;
	}
	
	public SiteDetails getSiteDetails(String date) throws Exception { 
		MySQL mysql = MySQL.getDreamhost();
		try (Statement s = mysql.getConnection().createStatement()) { 
			return SiteDetails.Builder.create()
					.withNumVisitors(getRowCount(s, "audits", date))
					.withNumProfiles(getRowCount(s, "audit_profiles", date))
					.withNumRecomms(getRowCount(s, "audit_recomms", date))
					.build();
		}
	}
	
	public ETLDetails getETLDetails(String date) throws Exception { 
	    MySQL mysql = MySQL.getDatabase("config/mysql-lh.properties");
	    try (Statement s = mysql.getConnection().createStatement()) { 
			ETLDetails.Builder builder = ETLDetails.Builder.create();

			
			return builder.build();
	    }
	}
	
	public JobDetails getJobDetails(String date) throws Exception { 
		String path = "/usr/local/taskforest/logs/" + date + "/";
		File folder = new File(path);
		File[] files = folder.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		    	// TODO replace with actuall test we want to perform...
		        return name.toLowerCase().endsWith(".txt");
		    }
		});
		
		List<String> failedJobs = Lists.newArrayList();
		for (File failedJob : files) { 
			// TODO parse the job file name and get the actual job name
			// from the family.job.success.fail
		}
		
		return JobDetails.Builder.create().withFailedJobs(failedJobs).build();
	}
	
	public String formatEmail(String date) throws Exception { 
		StringBuilder buf = new StringBuilder();
		buf.append("<strong>Site Details</strong>").append("\n").append("\n");

		SiteDetails siteDetails = getSiteDetails(date);
		buf.append("# of visitors: ").append(siteDetails.getNumVisitors()).append("\n");
		buf.append("# of profiles: ").append(siteDetails.getNumProfiles()).append("\n");
		buf.append("# of recomms: ").append(siteDetails.getNumRecomms()).append("\n");
		buf.append("\n");
		
		buf.append("<strong>ETL Information</strong>").append("\n").append("\n");
		
		ETLDetails etl = getETLDetails(date);
		buf.append("# of accounts updated: ").append(etl.getNumUpdated()).append("\n");
		buf.append("# of accounts first pull: ").append(etl.getNumPulled()).append("\n");
		buf.append("# of accounts found: ").append(etl.getNumFound()).append("\n");
		buf.append("\n");

		buf.append("# of friends added: ").append(etl.getNumFriends()).append("\n");
		buf.append("\n");
		
		buf.append("<strong>Failed Jobs</strong>").append("\n").append("\n");
		
		buf.append("<ul>").append("\n");
		JobDetails job = getJobDetails(date);
		for (String s : job.getFailedJobs()) {
			buf.append("<li>").append(s).append("\n");
		}
		buf.append("</ul>").append("\n");
		return buf.toString();
	}
	
	public static void main(String[] args) throws Exception { 
		SummaryEmail summary = new SummaryEmail();
		String body = summary.formatEmail("2012-02-02");

		Properties prop = new Properties();
		InputStream input = new FileInputStream("config/email.properties");
		prop.load(input);

		final String username = prop.getProperty("fromAddress");
		final String password = prop.getProperty("password");
		
		final String toAddress = prop.getProperty("toAddress");
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {

			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(toAddress));
			message.setSubject("[SteamRecommender] Report");
			message.setText(body, "utf-8", "html");

			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}	
	}
}
