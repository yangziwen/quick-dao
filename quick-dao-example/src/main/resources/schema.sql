create table `user` (
    `id` bigint primary key auto_increment,
    `username` varchar(32) not null,
    `email` varchar(64) not null,
    `gender` integer,
    `age` integer,
    `create_time` datetime,
    `update_time` datetime,
    `valid` boolean
);