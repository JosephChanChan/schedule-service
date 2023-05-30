package com.mixc.cpms.schedule.mq.service.dao;

import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import com.mixc.cpms.schedule.mq.service.model.DistributionLock;
import com.mixc.cpms.schedule.mq.service.model.dto.DelayedMsgDTO;
import com.mixc.cpms.schedule.mq.service.model.dto.LimitDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author Joseph
 * @since 2023/1/18
 */
@Mapper
public interface IDistributionLockMapper {

    Integer insert(@Param("lock") DistributionLock lock);

    DistributionLock getLockInfo(@Param("lockName") String lockName);

    Integer delLock(@Param("lockName") String lockName, @Param("lockOwner") String lockOwner);

}
