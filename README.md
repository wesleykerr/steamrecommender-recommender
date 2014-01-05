Deploy New Version
======

    mvn clean install
    mvn release:prepare
    mvn release:perform

The release is automatically moved over to the server.  Now ssh onto the server and update the symbolic
link in /usr/local/game-recommender to point to the new release found in 

    /repo/releases/com/wesleykerr/steam/recommender/<version>/recommender-<version>.jar



Gathering Steam Players
======

Find players who own a specific game.

    http://steamcommunity.com/actions/Search?T=Account&K=%22dota%22
    http://steamcommunity.com/actions/Search?T=Account&K=%22dota%22&p=2

Find players by looking through a steam community

    http://steamcommunity.com/games/Skyrim/members/
    http://steamcommunity.com/games/Skyrim/members/?p=2

All of the players are stored within the div - 

   div#memberList

## New Server Deploy (ubuntu)

### Libraries

git, openssh-client, openssh-server

### Github and Bitbucket

add new keys and register them to bypass entering your password with every commit, push, pull

### Taskforest

1.  Download source files from sourceforge.
2.  Install libconfig-general-perl, libdatetime-perl, liblog-log4perl-perl, liblwp-protocol-https-perl
3.  Follow INSTALL instructions in taskforest folder
3.  Clone taskforest repository

````
    git clone git@bitbucket.org:wesleykerr/taskforest.git
````

### Artifacts

Clone recommender-artifacts and make sure to put steam key config/recommender.properties.  Add symbolic
link from the latest recommender-0.0.x.jar to recommender.jar

### Couchbase

Remember to bacup the couchbase cluster before doing anything. 

Schedule weekly backups of couchbase so that we can restore if something goes wrong.  These should be uploaded
to dreamhost using rsync and that gives us off-site backups.

http://www.couchbase.com/docs/couchbase-manual-2.0/couchbase-backup-restore-mac.html

### Cron Jobs



