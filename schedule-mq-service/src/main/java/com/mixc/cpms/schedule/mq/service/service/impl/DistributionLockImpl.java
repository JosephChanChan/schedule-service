package com.mixc.cpms.schedule.mq.service.service.impl;

import com.mixc.cpms.schedule.mq.service.common.Constant;
import com.mixc.cpms.schedule.mq.client.kit.NumberKit;
import com.mixc.cpms.schedule.mq.client.kit.StringKit;
import com.mixc.cpms.schedule.mq.service.config.ApplicationConfig;
import com.mixc.cpms.schedule.mq.service.dao.IDistributionLockMapper;
import com.mixc.cpms.schedule.mq.service.model.DistributionLock;
import com.mixc.cpms.schedule.mq.service.service.IDistributionLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 基于数据库实现的分布式锁
 *
 * @author Joseph
 * @since 2023/2/7
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributionLockImpl implements IDistributionLockService {

    private final IDistributionLockMapper mapper;

    private final ApplicationConfig applicationConfig;


    @Override
    public void lock(String name, String owner, Integer validSeconds) {
        if (NumberKit.lte0(validSeconds)) {
            validSeconds = applicationConfig.getDistributionLockValidSec();
        }
        doLock(name, owner, validSeconds);
    }

    private void doLock(String name, String owner, Integer validSeconds) {
        DistributionLock model = new DistributionLock();
        model.setLockName(name);
        model.setLockOwner(owner);
        model.setValidTimes(validSeconds);

        while (!insertLock(model)) {

            DistributionLock lockInfo = mapper.getLockInfo(name);

            while (null != lockInfo && valid(lockInfo)) {
                log.info("DistributionLock still valid {}", name);

                sleep(Constant.THREAD_RACE_LOCK_INTERVAL_MILLIS);

                if (valid(lockInfo)) {
                    if (null == (lockInfo = mapper.getLockInfo(name))) {
                        break;
                    }
                }
            }
            if (null != lockInfo) {
                mapper.delLock(name, owner);
            }
        }
    }

    @Override
    public void unlock(String name, String owner) {
        if (StringKit.notBlank(name) && StringKit.notBlank(owner) ) {
            mapper.delLock(name, owner);
        }
    }

    private boolean insertLock(DistributionLock lock) {
        try {
            mapper.insert(lock);
            log.info("DistributionLock acquired lock! {}", lock.getLockName());
            return true;
        }
        catch (DuplicateKeyException e) {
            // 线程竞争失败
            log.warn("DistributionLock race fail! {}", lock.getLockName());
            return false;
        }
    }

    private boolean valid(DistributionLock lock) {
        // 锁的有效时间
        Integer validTimes = lock.getValidTimes();
        // 锁的创建时间
        LocalDateTime time = lock.getCreateTime();
        if (NumberKit.lte0(validTimes)) {
            validTimes = applicationConfig.getDistributionLockValidSec();
        }
        time = time.plusSeconds(validTimes);
        // 当前时间 < 锁到期时间 -> 锁仍有效
        return LocalDateTime.now().isBefore(time);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
