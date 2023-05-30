/*
	author: richard
	description: 数据库初始化脚本
	returns: nothing
*/

create table distribution_lock(
  `lock_name` varchar(50) not null primary key,
  `lock_owner` varchar(50) not null,
  `valid_times` int not null,
  `create_time` datetime not null default now()
);