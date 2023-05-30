package com.mixc.cpms.schedule.mq.service.job;

import com.mixc.cpms.schedule.mq.service.cache.ScheduleOffsetHolder;
import com.mixc.cpms.schedule.mq.service.cache.TimeBucketWheel;
import com.mixc.cpms.schedule.mq.service.common.ThreadFactoryImpl;
import com.mixc.cpms.schedule.mq.service.controller.ScheduleController;
import com.mixc.cpms.schedule.mq.service.model.ScheduleOffset;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时持久化存储schedule offset
 *
 * @author Joseph
 * @since 2023/2/6
 */
@Slf4j
public class FlushScheduleOffsetService {

    private final ScheduledExecutorService driver =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("FlushScheduleOffsetThread"));

    private ScheduleOffsetHolder scheduleOffsetHolder ;


    public void start(ScheduleOffsetHolder holder) {
        this.scheduleOffsetHolder = holder;

        log.info("FlushScheduleOffsetService beginning...");
        driver.scheduleAtFixedRate(new FlushScheduleOffsetJob(), 3, 3, TimeUnit.SECONDS);
        log.info("FlushScheduleOffsetService driver is on the way!");
    }

    public void stop() {
        driver.shutdown();
        // 服务退出前再刷一次进度
        scheduleOffsetHolder.flushOffset();
    }

    class FlushScheduleOffsetJob implements Runnable {

        @Override
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("FlushScheduleOffsetJob launching");
            }
            try {
                scheduleOffsetHolder.flushOffset();
            }
            catch (RuntimeException e) {
                log.error("FlushScheduleOffsetJob error!!!", e);
            }
        }
    }
}
