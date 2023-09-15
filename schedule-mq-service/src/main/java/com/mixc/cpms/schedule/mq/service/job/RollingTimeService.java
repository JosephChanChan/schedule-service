package com.mixc.cpms.schedule.mq.service.job;

import com.mixc.cpms.schedule.mq.service.cache.TimeBucketWheel;
import com.mixc.cpms.schedule.mq.service.common.ThreadFactoryImpl;
import com.mixc.cpms.schedule.mq.service.common.ThreadKit;
import com.mixc.cpms.schedule.mq.service.common.TimeKit;
import com.mixc.cpms.schedule.mq.service.controller.ScheduleController;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 负责时间轮滚动和发起时间轮切换
 *
 * @author Joseph
 * @since 2023/1/20
 */
@Slf4j
public class RollingTimeService {

    private final ScheduledExecutorService driver =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("RollingTimeThread"));

    private volatile TimeBucketWheel wheel ;

    private ScheduleController scheduleController ;

    private PreloadTimeSegmentService preloadTimeSegmentService ;

    public void start(TimeBucketWheel wheel,
                      ScheduleController controller,
                      PreloadTimeSegmentService preloadTimeSegmentService) {

        this.wheel = wheel;
        this.scheduleController = controller;
        this.preloadTimeSegmentService = preloadTimeSegmentService;

        log.info("RollingTimeService beginning...");
        driver.scheduleAtFixedRate(new RollingJob(), 1000, 100, TimeUnit.MILLISECONDS);
        log.info("RollingTimeService driver is on the way!");
    }

    public void stop() {
        driver.shutdown();
    }

    public void swapWheel(TimeBucketWheel nextWheel) {
        if (null != nextWheel) {
            log.info("RollingTimeService swapping TimeBucketWheel new timeBoundRight is {}", nextWheel.getTimeBoundRight());
        }
        this.wheel = nextWheel;
    }

    public boolean isTimeChange() {
        if (null == wheel) {
            return false;
        }
        return TimeKit.nowSeconds() > wheel.getTimeBoundRightSec();
    }

    class RollingJob implements Runnable {

        @Override
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("RollingJob launching");
            }
            if (null == wheel) {
                // 当前没有延迟消息
                if (log.isDebugEnabled()) {
                    log.debug("RollingJob wheel is null, no delay msg please check");
                }
                return;
            }
            try {
                wheel.roll();
                if (log.isDebugEnabled()) {
                    log.debug("RollingJob done");
                }
                if (isTimeChange()) {

                    // 必须等到下一个时间片被加载后才执行时间片切换
                    while (! scheduleController.hasWheelNext()) {
                        loadNextWheelQuickly();
                    }

                    long oldSegment = wheel.getTimeBoundRight();
                    scheduleController.swapWheel(RollingTimeService.this::swapWheel);
                    log.info("RollingTimeService swapping TimeBucketWheel done old segment={} new segment={}",
                            oldSegment, wheel.getTimeBoundRight());
                }
            }
            catch (RuntimeException e) {
                log.error("RollingJob error!!!", e);
            }
        }
    }

    private void loadNextWheelQuickly() {
        log.info("RollingTimeService loadNextWheelQuickly start");
        if (! preloadTimeSegmentService.markLoading()) {
            log.info("RollingTimeService loadNextWheelQuickly markLoading fail. maybe PreloadTimeSegmentService is working");
            // 先等一下到时间片被加载完成
            ThreadKit.sleep(100);
            return;
        }
        if (scheduleController.hasWheelNext()) {
            log.info("RollingTimeService loadNextWheelQuickly terminated cause hasWheelNext");
            return;
        }
        try {
            preloadTimeSegmentService.preloadNextWheel(wheel.getTimeBoundRight());
        }
        finally {
            preloadTimeSegmentService.markLoadDone();
        }
        log.info("RollingTimeService loadNextWheelQuickly done");
    }


}
