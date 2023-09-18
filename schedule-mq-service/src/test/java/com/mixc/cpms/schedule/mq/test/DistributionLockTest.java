package com.mixc.cpms.schedule.mq.test;

import cn.hutool.core.util.IdUtil;
import com.mixc.cpms.schedule.mq.service.ScheduleMqServiceApplication;
import com.mixc.cpms.schedule.mq.service.service.IDistributionLockService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Joseph
 * @since 2023/9/15
 */
@Slf4j
@SpringBootTest(classes = ScheduleMqServiceApplication.class)
public class DistributionLockTest {

    private final String lockName = "TEST_LOCK";

    @Autowired
    private IDistributionLockService distributionLockService;


    @Test
    public void lockTest() {
        String lockOwner = IdUtil.fastSimpleUUID();
        try {
            distributionLockService.lock(lockName, lockOwner, 60);
        }
        finally {
            distributionLockService.unlock(lockName, lockOwner);
        }
    }

    @Test
    public void overdueLockTest() {
        String lockOwner = IdUtil.fastSimpleUUID();
        String lockOwner2 = IdUtil.fastSimpleUUID();
        distributionLockService.lock(lockName, lockOwner, 60);
        try {
            distributionLockService.lock(lockName, lockOwner2, 60);
        }
        finally {
            distributionLockService.unlock(lockName, lockOwner);
            distributionLockService.unlock(lockName, lockOwner2);
        }
    }


}
