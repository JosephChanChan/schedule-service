package com.mixc.cpms.schedule.mq.service.job;

import com.mixc.cpms.schedule.mq.service.cache.TimeBucketWheel;
import com.mixc.cpms.schedule.mq.service.common.CollectionsKit;
import com.mixc.cpms.schedule.mq.service.common.Constant;
import com.mixc.cpms.schedule.mq.service.common.ThreadFactoryImpl;
import com.mixc.cpms.schedule.mq.service.controller.ScheduleController;
import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import com.mixc.cpms.schedule.mq.service.model.MsgItem;
import com.mixc.cpms.schedule.mq.service.model.dto.TimeSegmentDTO;
import com.mixc.cpms.schedule.mq.service.service.IMQDispatcher;
import com.mixc.cpms.schedule.mq.service.service.ITimeBucketService;
import com.mixc.cpms.schedule.mq.service.service.impl.MQDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 预加载时间片线程
 *
 * @author Joseph
 * @since 2023/1/25
 */
@Slf4j
public class PreloadTimeSegmentService {

    private final ScheduledExecutorService driver =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("PreloadTimeSegmentThread"));

    private IMQDispatcher mqDispatcher;

    private ScheduleController controller;

    private ITimeBucketService timeBucketService;


    public void start(ITimeBucketService timeBucketService, IMQDispatcher mqDispatcher, ScheduleController controller) {
        log.info("PreloadTimeSegmentService beginning...");
        this.controller = controller;
        this.mqDispatcher = mqDispatcher;
        this.timeBucketService = timeBucketService;
        driver.scheduleAtFixedRate(new PreloadTimeSegmentJob(), 10, 10, TimeUnit.SECONDS);
        log.info("PreloadTimeSegmentService driver is on the way!");
    }

    public void stop() {
        driver.shutdown();
    }

    public void preloadNextWheel(long nowWheelRightTimeBound, boolean hasWheelRolling) {
        log.info("PreloadTimeSegmentService preloadNextWheel nowWheelRightTimeBound={} hasWheelRolling={}",
                nowWheelRightTimeBound, hasWheelRolling);

        TimeSegmentDTO dto = timeBucketService.loadNextSegment(nowWheelRightTimeBound);
        if (null == dto) {
            log.info("PreloadTimeSegmentService no next wheel nowWheelRightTimeBound={} hasWheelRolling={}",
                    nowWheelRightTimeBound, hasWheelRolling);
            return;
        }

        TimeBucketWheel wheel = new TimeBucketWheel(nowWheelRightTimeBound, dto.getTimeBoundRight(), mqDispatcher);

        // 设置了wheel后，延迟消息就可以写入内存了
        if (! hasWheelRolling) {
            controller.setWheelRolling(wheel);
        }
        else {
            controller.setWheelNext(wheel);
        }

        List<DelayedMsg> delayedMsgList = dto.getDelayedMsgList();
        if (CollectionsKit.isNotEmpty(delayedMsgList)) {
            long maxId = delayedMsgList.get(delayedMsgList.size()-1).getId();

            wheel.initialize(delayedMsgList.stream().map(DelayedMsg::convert).collect(Collectors.toList()));

            Long minId = timeBucketService.maxIdFromSegment(wheel.getTimeBucket());
            log.info("PreloadTimeSegmentService nextWheel initialized get append msg from maxId={} to minId={}", maxId, minId);

            if (minId > maxId) {
                TimeSegmentDTO appendDTO = timeBucketService.loadSegmentInRange(wheel.getTimeBucket(), maxId, minId);
                List<DelayedMsg> list = appendDTO.getDelayedMsgList();
                List<MsgItem> appendList = list.stream().map(DelayedMsg::convert).collect(Collectors.toList());
                for (MsgItem msgItem : appendList) {
                    wheel.put(msgItem);
                }
                log.info("PreloadTimeSegmentService append msg done size={}", list.size());
            }
        }
    }

    class PreloadTimeSegmentJob implements Runnable {

        @Override
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("PreloadTimeSegmentJob launching");
            }
            if (controller.hasWheelRolling() && controller.hasWheelNext()) {
                return;
            }
            try {
                Long wheelRollingTime = controller.getWheelRollingTime();
                boolean hasWheelRolling = controller.hasWheelRolling();
                preloadNextWheel(wheelRollingTime, hasWheelRolling);
                if (log.isDebugEnabled()) {
                    log.debug("PreloadTimeSegmentJob done");
                }
            }
            catch (RuntimeException e) {
                log.error("PreloadTimeSegmentJob error!!!", e);
            }
        }
    }


}
