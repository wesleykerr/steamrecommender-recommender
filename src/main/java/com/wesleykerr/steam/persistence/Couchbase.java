package com.wesleykerr.steam.persistence;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import net.spy.memcached.PersistTo;
import net.spy.memcached.ReplicateTo;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.wesleykerr.steam.domain.player.Player;
import com.wesleykerr.steam.domain.player.Player.Builder;
import com.wesleykerr.utils.GsonUtils;

public class Couchbase {

    public static CouchbaseClient connect(String bucket) throws IOException, URISyntaxException { 
        List<URI> hosts = Arrays.asList(new URI("http://192.168.0.8:8091/pools"));
        return new CouchbaseClient(hosts, bucket, "");
    }
    
    public static void main(String[] args) throws Exception { 
        CouchbaseClient client = connect("default");
        
        View v = client.getView("tmp", "friends_found");
        Query q = new Query().setReduce(false).setIncludeDocs(true);
        ViewResponse response = client.query(v, q);
        for (ViewRow row : response) { 
            Player player = GsonUtils.getDefaultGson().fromJson(row.getValue(), Player.class);
            Player updated = Builder.create()
                    .withPlayer(player)
                    .withFriendsMillis(null)
                    .build();
            
            client.set(updated.getId(), GsonUtils.getDefaultGson().toJson(updated), PersistTo.MASTER, ReplicateTo.ONE).get();
            System.out.println(updated.getFriendsMillis());
        }

        client.shutdown();
    }
}
