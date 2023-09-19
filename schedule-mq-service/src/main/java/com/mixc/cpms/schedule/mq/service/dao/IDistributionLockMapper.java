package com.mixc.cpms.schedule.mq.service.dao;

import com.mixc.cpms.schedule.mq.service.model.DistributionLock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * @author Joseph
 * @since 2023/1/18
 */
@Mapper
public interface IDistributionLockMapper {

    Integer insert(@Param("lock") DistributionLock lock);

    DistributionLock getLockInfo(@Param("lockName") String lockName);

    Integer delLock(@Param("lockName") String lockName, @Param("lockOwner") String lockOwner);

    Integer expireLock(@Param("lockName") String lockName, @Param("lockOwner") String lockOwner);

}
