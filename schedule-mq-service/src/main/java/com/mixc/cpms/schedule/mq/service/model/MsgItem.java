package com.mixc.cpms.schedule.mq.service.model;

import lombok.Data;

/**
 * @author Joseph
 * @since 2023/1/19
 */
@Data
public class MsgItem {

    /**
     * ID
     */
    private Long id;

    /**
     * 到期执行时间，秒级
     */
    private Long deadlineSeconds;
}