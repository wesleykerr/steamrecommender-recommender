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

### Ruby

install RVM and ruby 1.9.3
install gems:
 
   gem install mysql2
   gem install httparty
   gem install nokogiri
   gem install damerau-levenshtein
 
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

http://www.couchbase.com/docs/couchbase-manual-2.0/couchbase-backup-restore-mac.html

### Backups

We have a taskforest script that runs once per week and assumes that we have our bitcasa drive mounted.  

    sudo mount -tbitcasa wesley.n.kerr@gmail.com /home/wkerr/bitcasa -o password=<bitcasa password>

Backup the couchbase server

    /opt/couchbase/bin/cbbackup http://<admin>:<password>@192.168.0.8:8091 /tmp/backup-12022013
    rsync -e ssh -av backup-12022013 b418667@hanjin.dreamhost.com:recommender 

Backup the database

    mysqldump -u recommender_etl -h mysql.seekerr.com -p --databases game_recommender > dreamhost_08172013.sql

### Cron Jobs

    05 00 * * * /usr/local/bin/taskforest --config_file=/usr/local/taskforest/config/taskforest.cfg


