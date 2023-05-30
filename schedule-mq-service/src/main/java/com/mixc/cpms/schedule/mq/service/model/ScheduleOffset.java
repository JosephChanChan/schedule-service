package com.mixc.cpms.schedule.mq.service.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author Joseph
 * @since 2023/1/18
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("schedule_offset")
public class ScheduleOffset {

    /*
        create table `schedule_offset`(
            `id` bigint UNSIGNED PRIMARY key auto_increment,
            `schedule_service_code` varchar(30) not null,
            `schedule_offset` varchar(50) not null,
            `update_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP(0)
        );
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("schedule_service_code")
    private String scheduleServiceCode;
    @TableField("schedule_offset")
    private String scheduleOffset;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
