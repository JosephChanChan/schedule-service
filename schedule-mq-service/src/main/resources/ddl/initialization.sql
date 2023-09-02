/*
author: richard
description: 数据库初始化脚本
returns: nothing
*/

CREATE TABLE `时间片` (
    `id` int unsigned NOT NULL AUTO_INCREMENT,
    `schedule_service_code` varchar(36) NOT NULL,
    `deadline` datetime NOT NULL,
    `topic` varchar(50) not null,
    `tags` varchar(50) not null default '',
    `msg_content` text NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `IDX_SERVICE_CODE` (`schedule_service_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

create table `schedule_offset`(
      `schedule_service_code` varchar(36) PRIMARY key,
      `time_segment` bigint UNSIGNED not null,
      `id_offset` int UNSIGNED not null,
      `update_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP(0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `heart_beat` (
  `schedule_service_code` varchar(36) NOT NULL,
  `report_time` datetime NOT NULL,
  PRIMARY KEY (`schedule_service_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `server_list` (
   `schedule_service_code` varchar(36) NOT NULL,
   `role` varchar(10) NOT NULL COMMENT '角色：master/slave',
   `slave_of` varchar(30) NOT NULL DEFAULT '' COMMENT 'master的servicecode',
   `expose_address` varchar(30) NOT NULL COMMENT '暴露给客户端调用的地址',
   `running_state` varchar(10) NOT NULL COMMENT '服务运行的状态',
   `create_time` datetime NOT NULL,
   `update_time` datetime DEFAULT NULL,
   PRIMARY KEY (`schedule_service_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

create table distribution_lock(
  `lock_name` varchar(50) not null primary key,
  `lock_owner` varchar(50) not null,
  `valid_times` int not null,
  `create_time` datetime not null default now()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;