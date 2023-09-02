package com.mixc.cpms.schedule.mq.service.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author Joseph
 * @since 2023/9/2
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("server_list")
public class ServerList {

    /*
        CREATE TABLE `server_list` (
          `schedule_service_code` varchar(36) NOT NULL,
          `role` varchar(10) NOT NULL COMMENT '角色：master/slave',
          `slave_of` varchar(30) NOT NULL DEFAULT '' COMMENT 'master的servicecode',
          `expose_address` varchar(30) NOT NULL COMMENT '暴露给客户端调用的地址',
          `running_state` varchar(10) NOT NULL COMMENT '服务运行的状态',
          `create_time` datetime NOT NULL,
          `update_time` datetime DEFAULT NULL,
          PRIMARY KEY (`service_code`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
     */

    @TableId(value = "schedule_service_code", type = IdType.INPUT)
    private String scheduleServiceCode;

    @TableField("role")
    private String role;

    @TableField("slave_of")
    private String slaveOf;

    @TableField("expose_address")
    private String exposeAddress;

    @TableField("running_state")
    private String runningState;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;


}
