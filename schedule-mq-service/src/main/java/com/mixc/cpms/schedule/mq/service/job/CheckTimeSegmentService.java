package com.mixc.cpms.schedule.mq.service.job;

import com.mixc.cpms.schedule.mq.service.cache.ScheduleOffsetHolder;
import com.mixc.cpms.schedule.mq.service.common.Constant;
import com.mixc.cpms.schedule.mq.service.common.DateTimeFormat;
import com.mixc.cpms.schedule.mq.service.common.ThreadFactoryImpl;
import com.mixc.cpms.schedule.mq.service.common.TimeKit;
import com.mixc.cpms.schedule.mq.service.controller.ScheduleController;
import com.mixc.cpms.schedule.mq.service.service.ITimeBucketService;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
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


    public void start(ITimeBucketService timeBucketService) {
        this.timeBucketService = timeBucketService;

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
                long newSegment = Long.parseLong(LocalDateTime.now().plusDays(30).format(DateTimeFormat.yyyyMMddHHmm));

                // 现在时间 + 30天 > maxSegment，可以创建新时间片了
                if (newSegment > maxSegment) {
                    try {
                        timeBucketService.createNewSegment(TimeKit.plus30Minutes(maxSegment));
                    }
                    catch (BadSqlGrammarException e) {
                        // 这里的做法有点粗暴，更好的办法是上分布式锁?
                        log.warn("CheckTimeSegmentJob createNewSegment fail errorMsg={}", e.getMessage());
                    }
                }
            }
            catch (RuntimeException e) {
                log.error("CheckTimeSegmentService error!!!", e);
            }
        }
    }
}
