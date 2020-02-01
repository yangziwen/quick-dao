create table `user` (
    `id` bigint primary key auto_increment,
    `username` varchar(32) not null unique,
    `email` varchar(64) not null unique,
    `gender` integer,
    `create_time` datetime,
    `update_time` datetime
);