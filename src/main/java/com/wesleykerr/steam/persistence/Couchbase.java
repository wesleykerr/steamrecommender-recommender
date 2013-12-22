package com.wesleykerr.steam.persistence;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import com.couchbase.client.CouchbaseClient;

public class Couchbase {

    public static CouchbaseClient connect(String bucket) throws IOException, URISyntaxException { 
        List<URI> hosts = Arrays.asList(new URI("http://192.168.0.8:8091/pools"));
        return new CouchbaseClient(hosts, bucket, "");
    }
    
    public static void main(String[] args) throws Exception { 
        CouchbaseClient client = connect("default");
        Object o = client.get("76561197960272967");
        System.out.println(o.toString());
        
        client.delete("to_delete").get();
        client.shutdown();
    }
}
