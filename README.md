Deploy New Version
======

    mvn clean install
    mvn release:prepare
    mvn release:perform

The release is automatically moved over to the server.  Now ssh onto the server and update the symbolic
link in /usr/local/game-recommender to point to the new release found in 

    /repo/releases/com/wesleykerr/steam/recommender/<version>/recommender-<version>.jar

If we are having ssh issues there are two things that you can do.  We need to make sure that the ssh program that we are using within maven can understand the known hosts file.  First I had to change a configuration setting in /etc/ssh/ssh_config so that the entries in the known_hosts file was not hashed.  Then I had to make sure that ssh used rsa instead of ecdsa.

   ssh -oHostKeyAlgorithms='ssh-rsa' host

    
On Server Restart
======

I may change this to automount bitcasa on every run of backups just to avoid worrying about this
in the future.

    sudo mount -tbitcasa wesley.n.kerr@gmail.com /home/wkerr/bitcasa -o password=<bitcasa password>
    nohup /usr/local/bin/taskforest --config_file=/usr/local/taskforest/config/taskforest.cfg &
    nohup /usr/local/bin/taskforestd --config_file=/usr/local/taskforest/config/taskforestd.cfg &

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

Remember to backup the couchbase cluster before doing anything. 

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

### MySQL Setup

MySQL connection from macbook-pro or other machines.

```
$ sudo nano /etc/mysql/my.cnf
```

and change the line:

```
bind-address           = localhost
```

to your own internal ip address e.g. 192.168.1.20

```
bind-address           = 192.168.1.20
```

Recent Ubuntu Server Editions (such as 10.04) ship with AppArmor and MySQL's profile might be in enforcing mode by default. You can check this by executing sudo aa-status like so:

    # sudo aa-status
    5 profiles are loaded.
    5 profiles are in enforce mode.
       /usr/lib/connman/scripts/dhclient-script
       /sbin/dhclient3
       /usr/sbin/tcpdump
       /usr/lib/NetworkManager/nm-dhcp-client.action
       /usr/sbin/mysqld
    0 profiles are in complain mode.
    1 processes have profiles defined.
    1 processes are in enforce mode :
       /usr/sbin/mysqld (1089)
    0 processes are in complain mode.

If mysqld is included in enforce mode, then it is the one probably denying the write. Entries would also be written in /var/log/messages when AppArmor blocks the writes/accesses. What you can do is edit /etc/apparmor.d/usr.sbin.mysqld and add /data/ and /data/* near the bottom like so:

````
    /usr/sbin/mysqld {
        ...
        /var/log/mysql/ r,
        /var/log/mysql/* rw,
        /var/run/mysqld/mysqld.pid w,
        /var/run/mysqld/mysqld.sock w,
        /data/steam/ r,
        /data/steam/* rw,
    }
````

And then make AppArmor reload the profiles.

    # sudo /etc/init.d/apparmor reload

WARNING: the change above will allow MySQL to read and write to the /data directory. We hope you've already considered the security implications of this.