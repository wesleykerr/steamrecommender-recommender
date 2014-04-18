package com.wesleykerr.steam.summary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.summary.domain.ETLDetails;
import com.wesleykerr.steam.summary.domain.JobDetails;
import com.wesleykerr.steam.summary.domain.SiteDetails;

public class SummaryEmail {
    private static final Logger LOGGER = LoggerFactory.getLogger(SummaryEmail.class);

	private int query(Statement s, String query) throws Exception { 
		try (ResultSet rs = s.executeQuery(query)) { 
			if (rs.next()) { 
				return rs.getInt(1);
			}
		}
		return 0;
	}
	
	private int getRowCount(Statement s, String tableName, String date) throws Exception {
		String query = "select count(0) from " + tableName + 
				" where create_datetime >= '" + date + "';";
		return query(s, query);
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

			builder.withPlayerCount(query(s, 
					"select count(0) from steam_data.players "
					+ "where revision > 0 and private = 0"));
			
			builder.withPrivateCount(query(s, 
					"select count(0) from steam_data.players "
					+ "where private = 1"));
			
			builder.withNewPlayersCount(query(s, 
					"select count(0) from steam_data.players "
					+ "where revision = 0"));
			
			builder.withNumUpdated(query(s, 
					"select count(0) from steam_data.players "
					+ "where revision > 1 and last_updated >= '" + date + "'"));
			
			builder.withNumPulled(query(s, 
					"select count(0) from steam_data.players "
					+ "where revision = 1 and last_updated >= '" + date + "'"));
			
			builder.withNumFriends(query(s, 
					"select count(0) from steam_data.friends "
					+ "where last_updated >= '" + date + "'"));
			
			builder.withSampledPrivate(query(s,
					"select count(0) from steam_data.players_sample "
					+ "where last_updated >= '" + date + "'" 
					+ "and private = 1"));
			
			builder.withSampledPrivate(query(s,
					"select count(0) from steam_data.players_sample "
					+ "where last_updated >= '" + date + "'" 
					+ "and private = 0"));

			return builder.build();
	    }
	}
	
	public JobDetails getJobDetails(String date) throws Exception { 
		String path = "/usr/local/taskforest/logs/" + date.replaceAll("[-]", "") + "/";
		File folder = new File(path);
		File[] files = folder.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		    	boolean accept = name.toLowerCase().matches(".*[.][1-9]+$");
		        return accept;
		    }
		});
		
		List<String> failedJobs = Lists.newArrayList();
		for (File failedJob : files) { 
			String[] tokens = failedJob.getName().split("[.]");
			failedJobs.add(tokens[1]);
		}
		Collections.sort(failedJobs);
		return JobDetails.Builder.create().withFailedJobs(failedJobs).build();
	}
	
	
	private String toHTMLList(List<String> keys, List<Integer> values) { 
		Preconditions.checkArgument(keys.size() == values.size());
		StringBuilder buf = new StringBuilder();
		buf.append("<ul>").append("\n");
		for (int i = 0; i < keys.size(); ++i) { 
			buf.append("<li><strong>").append(keys.get(i)).append("</strong>:");
			buf.append(values.get(i)).append("\n");
		}
		buf.append("</ul>").append("\n");
		return buf.toString();
	}
	
	private String toHTMLHeader(String name) { 
		StringBuilder buf = new StringBuilder();
		buf.append("<h2>").append(name).append("</h2>");
		buf.append("\n").append("\n");
		return buf.toString();
	}
	
	public String formatEmail(String date) throws Exception { 
		StringBuilder buf = new StringBuilder();
		buf.append(toHTMLHeader("Site Details"));

		SiteDetails siteDetails = getSiteDetails(date);
		buf.append(toHTMLList(
				Lists.newArrayList("# of visitors", "# of profiles", "# of recomms"),
				Lists.newArrayList(
						siteDetails.getNumVisitors(), 
						siteDetails.getNumProfiles(), 
						siteDetails.getNumRecomms())
		));
		
		buf.append(toHTMLHeader("ETL Information"));
		
		ETLDetails etl = getETLDetails(date);
		
		
		buf.append(toHTMLList(
				Lists.newArrayList(
						"Total Player Count", 
						"New Player Count", 
						"Private Player Count", 
						"Sampled",
						"Private Ratio"),
				Lists.newArrayList(
						etl.getPlayerCount(), 
						etl.getNewPlayersCount(), 
						etl.getPrivateCount(),
						etl.getSampledPrivate()+etl.getSampledPublic(),
						etl.getSampledPrivate() / 
							(etl.getSampledPublic() + etl.getSampledPrivate()))
		));
									
		buf.append(toHTMLList(
				Lists.newArrayList(
						"# of accounts updated", 
						"# of accounts first pull", 
						"# of accounts found",
						"# of friends added"),
				Lists.newArrayList(
						etl.getNumUpdated(), 
						etl.getNumPulled(), 
						etl.getNumFound(), 
						etl.getNumFriends())
		));
				
		JobDetails job = getJobDetails(date);
		if (!job.getFailedJobs().isEmpty()) {
			buf.append(toHTMLHeader("Failed Jobs"));
			buf.append("<ul>").append("\n");
			for (String s : job.getFailedJobs()) {
				buf.append("<li>").append(s).append("\n");
			}
			buf.append("</ul>").append("\n");
		}
		return buf.toString();
	}
	
	public static void main(String[] args) throws Exception { 
        if (args.length != 1) {
            System.out.println("Usage: SummaryEmail <date>");
            System.exit(0);
        }
        String date = args[0];
		
		SummaryEmail summary = new SummaryEmail();
		String body = summary.formatEmail(date);

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
			message.setSubject("[SteamRecommender] " + date + " Report");
			message.setText(body, "utf-8", "html");

			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}	
	}
}
