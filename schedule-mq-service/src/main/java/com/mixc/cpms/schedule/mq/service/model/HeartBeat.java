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
@TableName("heart_beat")
public class HeartBeat {

    /*
        CREATE TABLE `heart_beat` (
          `schedule_service_code` varchar(36) NOT NULL,
          `report_time` datetime NOT NULL,
          PRIMARY KEY (`schedule_service_code`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
     */

    @TableId(value = "schedule_service_code", type = IdType.INPUT)
    private String scheduleServiceCode;

    @TableField("report_time")
    private Date reportTime;
}
