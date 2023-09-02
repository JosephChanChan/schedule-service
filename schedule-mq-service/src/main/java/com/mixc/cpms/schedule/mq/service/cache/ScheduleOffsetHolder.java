package com.mixc.cpms.schedule.mq.service.cache;

import com.mixc.cpms.schedule.mq.service.common.Constant;
import com.mixc.cpms.schedule.mq.service.common.ThreadFactoryImpl;
import com.mixc.cpms.schedule.mq.service.config.ApplicationConfig;
import com.mixc.cpms.schedule.mq.service.service.IScheduleOffsetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 调度偏移量缓存器
 *
 * @author Joseph
 * @since 2023/2/4
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleOffsetHolder {

    /**
     * 当前时间片
     */
    private long nowTimeSegment ;

    /**
     * 上一次保存成功的偏移量
     */
    private int lastFlushOffset = 0;

    /**
     * MQ消息分派器线程池的每个线程更新自己的投递offset
     */
    private int[] offsetTables = new int[Constant.MQ_DISPATCHER_CORE_THREADS];

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final IScheduleOffsetService offsetService;

    private final ApplicationConfig applicationConfig;


    public void initialize(long timeSegment) {
        log.info("ScheduleOffsetHolder initialize begin");
        lock.writeLock().lock();
        this.nowTimeSegment = timeSegment;
        lock.writeLock().unlock();
        log.info("ScheduleOffsetHolder initialize done");
    }

    /**
     * 切换时间片，更新到下一个时间片
     */
    public void nextTime(long timeSegment) {
        log.info("ScheduleOffsetHolder nextTime attempt to race lock");
        lock.writeLock().lock();
        log.info("ScheduleOffsetHolder nextTime acquired write lock!");

        try {
            // 将当前的offset刷盘
            flushOffset();

            // 更新时间片和offset table
            this.nowTimeSegment = timeSegment;
            this.offsetTables = new int[Constant.MQ_DISPATCHER_CORE_THREADS];
            this.lastFlushOffset = 0;
        }
        finally {
            log.info("ScheduleOffsetHolder nextTime update done");
            lock.writeLock().unlock();
        }
    }

    /**
     * 更新时间片的最新推进offset
     */
    public void updateOffset(long time, int offset) {
        if (time != nowTimeSegment) {
            return;
        }
        Thread thread = Thread.currentThread();
        if (!(thread instanceof ThreadFactoryImpl.AdvisedThread)) {
            return;
        }
        ThreadFactoryImpl.AdvisedThread advisedThread = (ThreadFactoryImpl.AdvisedThread) thread;
        int threadIndex = advisedThread.getThreadIndex();

        lock.readLock().lock();
        try {
            if (time == nowTimeSegment) {
                int oldOffset = offsetTables[threadIndex];
                offsetTables[threadIndex] = Math.max(oldOffset, offset);
            }
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void flushOffset() {
        if (log.isDebugEnabled()) {
            log.debug("ScheduleOffsetHolder flushOffset attempt to race lock");
        }
        lock.writeLock().lock();
        if (log.isDebugEnabled()) {
            log.debug("ScheduleOffsetHolder flushOffset acquired write lock!");
        }
        try {
            int max = -1;
            for (int offset : offsetTables) {
                max = Math.max(max, offset);
            }
            if (max <= lastFlushOffset) {
                return;
            }
            if (offsetService.updateOffset(applicationConfig.getScheduleServiceCode(), nowTimeSegment, max)) {
                lastFlushOffset = max;
            }
        }
        finally {
            if (log.isDebugEnabled()) {
                log.debug("ScheduleOffsetHolder flushOffset done");
            }
            lock.writeLock().unlock();
        }
    }



}
