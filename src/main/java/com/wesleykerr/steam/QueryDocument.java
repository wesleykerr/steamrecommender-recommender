package com.wesleykerr.steam;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wesleykerr.steam.persistence.dao.CounterDAO;
import com.wesleykerr.steam.persistence.memory.CounterDAOImpl;
import com.wesleykerr.utils.Utils;

/**
 * We are requiring that the QueryDocument
 * manage the number of documents that it
 * is requesting.
 * @author wkerr
 *
 */
public class QueryDocument {
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryDocument.class);
	private static final int TIMEOUT = 120;

	private CounterDAO counter;
	
	private HttpClient httpClient;
	
	public QueryDocument(CounterDAO counter) {
		this.counter = counter;
		this.httpClient = new DefaultHttpClient();
		this.httpClient.getParams().setParameter("http.socket.timeout", TIMEOUT*1000);
		this.httpClient.getParams().setParameter("http.connection.timeout", TIMEOUT*1000);
		this.httpClient.getParams().setParameter("http.connection-manager.timeout", new Long(TIMEOUT*1000));
		this.httpClient.getParams().setParameter("http.protocol.head-body-timeout", TIMEOUT*1000);
	}
	
	/**
	 * Gather the document from the web.
	 * @param url
	 * @param maxRetries
	 * @return
	 */
	public Document request(String url, int maxRetries) { 
		int retries = 0;
		Document doc = null;
		do {
			try {
				doc = Jsoup.connect(url).get();
			} catch (IOException e) { 
				LOGGER.warn(e.getMessage());
				++retries;
				Utils.delay(retries*1000);
			}
		} while (doc == null && retries < maxRetries);
		counter.incrCounter();
		return doc;
	}
	
	/**
	 * Gather a document from the web expected to be in 
	 * JSON format.
	 * @param uri
	 * @return
	 */
	public JsonObject requestJSON(URI uri, int maxRetries) {     	
		int retries = 0;
    	JsonObject jsonObj = null;
		do { 
			try { 
	        	HttpGet httpget = new HttpGet(uri);
	        	httpget.setHeader("User-Agent", "steamrecommender.com");
	        	HttpResponse response = httpClient.execute(httpget);
	        	counter.incrCounter();

	        	if (response.getStatusLine().getStatusCode() == 500) {
	        	    EntityUtils.consume(response.getEntity());
	        		LOGGER.warn("Server Error: " + response.toString());
					++retries;
					Utils.delay(retries*1000);
	        	} else { 
		        	HttpEntity entity = response.getEntity();
		 	       
		        	if (entity != null) { 
		            	try { 
		            	    JsonParser parser = new JsonParser();
		            	    JsonElement element = parser.parse(new InputStreamReader(entity.getContent()));
		            		if (element == null) {
		        	    		LOGGER.error("ERROR - empty content" + response.toString());
		            			return null;
		            		}
		            		jsonObj = element.getAsJsonObject();
		        		} finally { 
		        			entity.getContent().close();
		        		}
		        	}
	        	}
	    	} catch (Exception e) { 
	    		LOGGER.error("requestJSON " + e.getMessage());
				++retries;
				Utils.delay(retries*1000);
	    	}
			
			LOGGER.info("jsonObj: " + jsonObj);
		} while (jsonObj == null && retries < maxRetries);
		return jsonObj;
	}
	
	public static void main(String[] args) { 
		System.setProperty("steam.key", "72A809B286ED454CC53C4D03EF798EE4");
		
		CounterDAO counterDAO = new CounterDAOImpl();
		QueryDocument doc = new QueryDocument(counterDAO);
		try {
			doc.requestJSON(new URI("http://api.steampowered.com/"), 2);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
