--liquibase formatted sql

--changeset wkerr:1
create table players (
    steamid bigint(20) not null primary key,
    revision int(11),
    num_games int(11),
    private bit(1),
    last_updated datetime,
    last_updated_friends datetime,
    content longtext 
);

