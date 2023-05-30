package com.mixc.cpms.schedule.mq.service.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Joseph
 * @since 2023/2/7
 */
@Data
public class DistributionLock {

    /**
     * ID
     */
    private Integer id;

    /**
     * 锁名
     * 建立唯一索引
     */
    private String lockName;

    /**
     * 锁的拥有者
     */
    private String lockOwner;

    /**
     * 锁的有效时间，现在默认为秒级
     */
    private Integer validTimes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
