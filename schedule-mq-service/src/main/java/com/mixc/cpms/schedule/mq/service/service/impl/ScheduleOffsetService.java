package com.mixc.cpms.schedule.mq.service.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mixc.cpms.schedule.mq.service.dao.IScheduleOffsetMapper;
import com.mixc.cpms.schedule.mq.service.model.ScheduleOffset;
import com.mixc.cpms.schedule.mq.service.service.IScheduleOffsetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author Joseph
 * @since 2023/1/18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleOffsetService implements IScheduleOffsetService {

    private final IScheduleOffsetMapper scheduleOffsetMapper;


    @Override
    public ScheduleOffset getOffset(String serviceCode) {
        log.info("getOffset serviceCode={}", serviceCode);

        ScheduleOffset scheduleOffset = scheduleOffsetMapper.selectOne(
                Wrappers.<ScheduleOffset>lambdaQuery().eq(ScheduleOffset::getScheduleServiceCode, serviceCode));
        log.info("scheduleOffset={}", scheduleOffset);
        return scheduleOffset;
    }

    @Override
    public boolean updateOffset(String serviceCode, Long segment, Long id) {
        ScheduleOffset model = new ScheduleOffset();
        model.setScheduleOffset(segment+"-"+id);
        int update = scheduleOffsetMapper.update(model,
                Wrappers.<ScheduleOffset>lambdaUpdate().eq(ScheduleOffset::getScheduleServiceCode, serviceCode));

        if (log.isDebugEnabled()) {
            log.debug("updateOffset serviceCode={} segment={} id={} res={}", serviceCode, segment, id, update);
        }

        if (update == 0) {
            model.setScheduleServiceCode(serviceCode);
            model.setUpdateTime(LocalDateTime.now());
            int insert = scheduleOffsetMapper.insert(model);
            log.info("updateOffset insert res={}", insert);
            return insert > 0;
        }
        return update > 0;
    }
}
