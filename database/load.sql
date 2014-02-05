LOAD DATA CONCURRENT LOCAL INFILE '/tmp/mysql-players.csv'     
INTO TABLE steam_data.audit_players     
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\''     
LINES TERMINATED BY '\n' 
(steamid, revision, num_games, @private, last_updated, last_updated_friends, content)     
SET private = cast(@private as signed);


LOAD DATA CONCURRENT LOCAL INFILE '/tmp/mysql-friends.csv'     
INTO TABLE steam_data.audit_friends    
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\''     
LINES TERMINATED BY '\n' 
(steamid, revision, num_friends, last_updated, content);     

