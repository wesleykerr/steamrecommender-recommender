package com.wesleykerr.steam;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lightcouch.CouchDbClient;

/**
 * This class is responsible for pulling down the
 * application data and storing it into the data
 * store so that it can be presented properly by the
 * web application.
 * @author wkerr
 *
 */
public class AppList {

	private static final String host_ = "api.steampowered.com";
	private static final String apiKey_ = "72A809B286ED454CC53C4D03EF798EE4";

	public AppList() { 
		
	}

    public static void appsToTSV(String outputFile) throws Exception { 
    	BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
    	
    	HttpClient httpClient = new DefaultHttpClient();
    	
    	URIBuilder builder = new URIBuilder();
    	builder.setScheme("http").setHost(host_).setPath("/ISteamApps/GetAppList/v0002/");
    	HttpGet httpget = new HttpGet(builder.build());
    	HttpResponse response = httpClient.execute(httpget);
    	System.out.println("Response: " + response.toString());
    	HttpEntity entity = response.getEntity();
    	if (entity != null) { 
        	System.out.println("Entity: " + entity.getContentLength());
    		try { 
        		Object obj = JSONValue.parse(new InputStreamReader(entity.getContent()));
        		JSONObject jsonObj = (JSONObject) obj;
        		
        		JSONArray apps = (JSONArray) ((JSONObject) jsonObj.get("applist")).get("apps");
        		for (Object o : apps) { 
        			JSONObject appObj = (JSONObject) o;
        			String name = (String) appObj.get("name");
        			Long id = (Long) appObj.get("appid");
        			
        			out.write(id + "\t" + name + "\n");
        		}
        		
    		} finally {
    			entity.getContent().close();
    			out.close();
    		}
    	}
    }
    
	public static void main(String[] args) throws Exception { 
		CouchDbClient dbClient = new CouchDbClient();
		appsToTSV("apps.tsv");
	}

}
