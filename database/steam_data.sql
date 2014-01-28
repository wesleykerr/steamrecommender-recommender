--liquibase formatted sql

--changeset wkerr:1
create table steam_data.players (
    steamid bigint(20) not null primary key,
    revision int(11),
    num_games int(11),
    private bit(1),
    last_updated datetime,
    last_updated_friends datetime,
    content longtext 
);

alter table steam_data.players 
add index refresh_idx (private ASC, last_updated ASC) ;

alter table steam_data.players 
add index new_players_idx (last_updated ASC) ;

alter table steam_data.players 
add index new_friends_idx (last_updated_friends ASC) ;

create table steam_data.friends (
    steamid bigint(20) not null primary key,
    revision int(11),
    num_friends int(11),
    last_updated datetime,
    content longtext
);

