package com.mixc.cpms.schedule.mq.service.model.dto;

import lombok.Data;

/**
 * @author Joseph
 * @since 2023/1/18
 */
@Data
public class LimitDTO {

    private long start;

    private long size;

    private long fromId;

    private long toId;
}
