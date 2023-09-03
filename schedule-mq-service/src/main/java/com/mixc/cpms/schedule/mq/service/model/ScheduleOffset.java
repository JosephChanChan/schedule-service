package com.mixc.cpms.schedule.mq.service.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author Joseph
 * @since 2023/1/18
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("schedule_offset")
public class ScheduleOffset {

    /*
        create table `schedule_offset`(
            `schedule_service_code` varchar(30) PRIMARY key,
            `time_segment` bigint UNSIGNED not null,
            `id_offset` int UNSIGNED not null,
            `update_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP(0)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
     */

    @TableId(value = "schedule_service_code", type = IdType.INPUT)
    private String scheduleServiceCode;

    @TableField("time_segment")
    private Long timeSegment;

    @TableField("id_offset")
    private Integer idOffset;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
