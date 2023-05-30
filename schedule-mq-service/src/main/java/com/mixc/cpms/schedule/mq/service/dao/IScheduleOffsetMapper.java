package com.mixc.cpms.schedule.mq.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mixc.cpms.schedule.mq.service.model.ScheduleOffset;
import org.apache.ibatis.annotations.Mapper;


/**
 * @author Joseph
 * @since 2023/1/18
 */
@Mapper
public interface IScheduleOffsetMapper extends BaseMapper<ScheduleOffset> {

}
