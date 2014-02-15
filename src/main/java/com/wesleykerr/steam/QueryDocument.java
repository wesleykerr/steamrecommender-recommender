package com.wesleykerr.steam;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
	
	private CloseableHttpClient httpClient;
	private RequestConfig requestConfig;
	
	public QueryDocument() { 
	    this(new CounterDAOImpl());
	}
	
	public QueryDocument(CounterDAO counter) {
		this.counter = counter;
		this.httpClient = HttpClients.createDefault();
		this.requestConfig = RequestConfig.custom()
		        .setSocketTimeout(TIMEOUT*1000)
		        .setConnectTimeout(TIMEOUT*1000)
		        .setConnectionRequestTimeout(TIMEOUT*1000)
		        .build();
	}
	
	/**
	 * Check to see if a document exists on the web.
	 * @param url
	 * @param maxRetries
	 * @return
	 */
	public boolean checkIfExists(String url, String userAgent, int maxRetries) { 
	    int retries = 0;
        do { 
            try { 
                HttpGet httpget = new HttpGet(url);
                httpget.setConfig(requestConfig);
                httpget.addHeader("User-Agent", userAgent);
                try (CloseableHttpResponse response = httpClient.execute(httpget)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        return true;
                    } 
                    
                    LOGGER.info("Failed to get " + url);
                    LOGGER.info("Recieved: " + response.getStatusLine().toString());
                    ++retries;
                } 
            } catch (Exception e) { 
                LOGGER.error("Failed...." + e.getMessage(), e);
                ++retries;
            }
        } while (retries < maxRetries);
        return false;
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
	public JsonObject requestJSON(URI uri, String userAgent, int maxRetries) {     	
		int retries = 0;
    	JsonObject jsonObj = null;
		do { 
			try { 
	        	HttpGet httpget = new HttpGet(uri);
	        	httpget.setHeader("User-Agent", userAgent);
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
	    		LOGGER.error("requestJSON " + e.getMessage(), e);
				++retries;
				Utils.delay(retries*1000);
	    	}
		} while (jsonObj == null && retries < maxRetries);
		return jsonObj;
	}
}
