create table `user` (
    `id` bigint primary key auto_increment,
    `username` varchar(32) not null,
    `email` varchar(64) not null,
    `gender` integer,
    `city` varchar(64),
    `age` integer,
    `create_time` datetime,
    `update_time` datetime,
    `valid` boolean
);

create table `user_role` (
    `id` bigint primary key auto_increment,
    `username` varchar(32) not null,
    `role_name` varchar(64) not null,
    `create_time` datetime,
    `update_time` datetime,
);