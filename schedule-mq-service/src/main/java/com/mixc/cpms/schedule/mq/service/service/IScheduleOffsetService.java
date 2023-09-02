package com.mixc.cpms.schedule.mq.service.service;

import com.mixc.cpms.schedule.mq.service.model.ScheduleOffset;

/**
 * @author Joseph
 * @since 2023/1/18
 */
public interface IScheduleOffsetService {

    ScheduleOffset getOffset(String serviceCode);

    boolean updateOffset(String serviceCode, Long segment, Integer id);
}
