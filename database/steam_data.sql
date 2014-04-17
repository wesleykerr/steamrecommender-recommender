--liquibase formatted sql

--changeset wkerr:1
create table steam_data.audit_players (
    id bigint(20) not null primary key auto_increment,
    steamid bigint(20) not null,
    revision int(11),
    num_games int(11),
    private bit(1),
    last_updated datetime,
    last_updated_friends datetime,
    content longtext 
);

alter table steam_data.audit_players 
add index players_view_idx (steamid ASC, revision ASC, last_updated ASC) ;

alter table steam_data.audit_players 
add index steamid_idx (steamid ASC) ;

alter table steam_data.audit_players 
add index last_updated_idx (last_updated ASC) ;

alter table steam_data.audit_players 
add index last_updated_friends_idx (last_updated_friends ASC) ;

alter table steam_data.audit_players 
add index revision_idx (revision ASC) ;


CREATE VIEW steam_data.players AS
SELECT t1.*
FROM audit_players AS t1
LEFT OUTER JOIN audit_players AS t2
  ON t1.steamid = t2.steamid 
        AND (t1.last_updated < t2.last_updated 
         OR (t1.last_updated = t2.last_updated AND t1.revision < t2.revision))
WHERE t2.steamid IS NULL;




create table steam_data.audit_friends (
    id bigint(20) not null primary key auto_increment,
    steamid bigint(20),
    revision int(11),
    num_friends int(11),
    last_updated datetime,
    content longtext
);

alter table steam_data.audit_friends 
add index steamid_idx (steamid ASC) ;

CREATE VIEW steam_data.friends AS
SELECT t1.*
FROM audit_friends AS t1
LEFT OUTER JOIN audit_friends AS t2
  ON t1.steamid = t2.steamid 
        AND (t1.last_updated < t2.last_updated 
         OR (t1.last_updated = t2.last_updated AND t1.revision < t2.revision))
WHERE t2.steamid IS NULL;

--changeset wkerr:2
create table steam_data.players_sample (
    id bigint(20) not null primary key auto_increment,
    steamid bigint(20) not null,
    num_games int(11),
    private bit(1),
    last_updated datetime not null,
    content longtext 
);
