package com.wesleykerr.steam.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.wesleykerr.steam.persistence.Couchbase;

public class CouchbaseExport {
    private static final Logger LOGGER = Logger.getLogger(CouchbaseExport.class);

    public static void run(String bucket, String viewName, File outputFile) throws Exception { 
        CouchbaseClient client = Couchbase.connect(bucket);
        
        int count = 0;
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(outputFile));
                Writer writer = new OutputStreamWriter(gzipOutputStream, "UTF-8");
                BufferedWriter output = new BufferedWriter(writer)) { 

            View v = client.getView("players", viewName);
            Query q = new Query().setReduce(false);
            ViewResponse response = client.query(v, q);
            for (ViewRow row : response) { 
                Object obj = client.get(row.getKey());
                output.write(obj.toString());
                output.write("\n");
                
                ++count;
                if (count % 10000 == 0)
                    LOGGER.info("processed " + count + " players");
                
                if (count > 100)
                    break;
            }
        } 
        
        LOGGER.info("processed " + count + " players");
        client.shutdown();
    }
    
    public static void main(String[] args) throws Exception { 
//        run("default", "active_players", new File("/tmp/training-data.gz"));
        run("default", "all_keys", new File("/tmp/players.gz"));
//        export.run("friends", "all_keys", new File("/tmp/friends.gz"));
    }
}
