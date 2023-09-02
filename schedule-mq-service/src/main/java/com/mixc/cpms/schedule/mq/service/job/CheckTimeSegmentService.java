package com.mixc.cpms.schedule.mq.service.job;

import cn.hutool.core.util.IdUtil;
import com.mixc.cpms.schedule.mq.service.common.Constant;
import com.mixc.cpms.schedule.mq.service.common.DateTimeFormat;
import com.mixc.cpms.schedule.mq.service.common.ThreadFactoryImpl;
import com.mixc.cpms.schedule.mq.service.common.TimeKit;
import com.mixc.cpms.schedule.mq.service.service.IDistributionLockService;
import com.mixc.cpms.schedule.mq.service.service.ITimeBucketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时检查时间片，负责创建和销毁
 *
 * @author Joseph
 * @since 2023/2/6
 */
@Slf4j
public class CheckTimeSegmentService {

    private final ScheduledExecutorService driver =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("CheckTimeSegmentThread"));

    private ITimeBucketService timeBucketService;

    private IDistributionLockService distributionLockService;


    public void start(ITimeBucketService timeBucketService, IDistributionLockService distributionLockService) {
        this.timeBucketService = timeBucketService;
        this.distributionLockService = distributionLockService;

        log.info("CheckTimeSegmentService beginning...");
        driver.scheduleAtFixedRate(new CheckTimeSegmentJob(), 10, 10, TimeUnit.SECONDS);
        log.info("CheckTimeSegmentService driver is on the way!");
    }

    public void stop() {
        driver.shutdown();
    }

    class CheckTimeSegmentJob implements Runnable {

        @Override
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("CheckTimeSegmentService launching");
            }
            try {
                List<Long> segments = timeBucketService.showAllSegments();
                Long maxSegment = segments.stream().max(Comparator.naturalOrder()).orElse(0L);
                long newSegment = Long.parseLong(LocalDateTime.now().plusDays(Constant.MAX_DELAYED_DAYS)
                        .format(DateTimeFormat.yyyyMMddHHmm));

                // 现在时间 + 30天 > maxSegment，可以创建新时间片了
                if (newSegment > maxSegment) {

                    String lockOwner = IdUtil.fastSimpleUUID();
                    distributionLockService.lock(Constant.CREATE_NEW_SEGMENT_DIS_LOCK, lockOwner, 10);

                    try {
                        timeBucketService.createNewSegment(TimeKit.plus30Minutes(maxSegment));
                    }
                    catch (BadSqlGrammarException e) {
                        // 上分布式锁后不该出现的问题
                        log.error("CheckTimeSegmentJob createNewSegment fail", e);
                    }
                    finally {
                        distributionLockService.unlock(Constant.CREATE_NEW_SEGMENT_DIS_LOCK, lockOwner);
                    }
                }
            }
            catch (RuntimeException e) {
                log.error("CheckTimeSegmentService error!!!", e);
            }
        }
    }
}
