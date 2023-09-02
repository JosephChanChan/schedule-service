package com.mixc.cpms.schedule.mq.service.cache;

import com.mixc.cpms.schedule.mq.service.common.CollectionsKit;
import com.mixc.cpms.schedule.mq.service.common.TimeKit;
import com.mixc.cpms.schedule.mq.service.enums.ErrorCode;
import com.mixc.cpms.schedule.mq.service.exception.BusinessException;
import com.mixc.cpms.schedule.mq.service.model.MsgItem;
import com.mixc.cpms.schedule.mq.service.model.TimeBucket;
import com.mixc.cpms.schedule.mq.service.service.IMQDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Joseph
 * @since 2023/1/19
 */
@Slf4j
public class TimeBucketWheel {

    /*
        时间轮，60个bucket，每个bucket为1s，bucket内有双向链表，链表元素是延迟msg的抽象
        提供批量写入、单个写入、读取当前T秒的可执行元素
     */

    /**
     * 时间轮指针
     */
    private int tick = 0;

    /**
     * 最近一次触发时间，秒级
     */
    private long lastTriggerSec = -1;

    /**
     * 时间轮一个轮回的tick数
     */
    private final int round = 60;

    /**
     * 时间左边界，不包含
     */
    private final long timeBoundLeft ;

    private final long timeBoundLeftSec ;

    /**
     * 时间右边界，包含
     */
    private final long timeBoundRight ;

    private final String timeBucket ;

    private final long timeBoundRightSec ;

    /**
     * 时间轮，共有round个刻度
     */
    private TimeBucket[] buckets = new TimeBucket[round];

    /**
     * 时间轮缓冲队列
     */
    private BlockingQueue<MsgItem> bufferQueue = new LinkedBlockingQueue<>();

    /**
     * 延时消息分派器
     */
    private final IMQDispatcher mqDispatcher;


    public TimeBucketWheel(Long leftTime, Long rightTime, IMQDispatcher mqDispatcher) {
        this.timeBoundLeft = leftTime;
        this.timeBoundLeftSec = TimeKit.convertSeconds(leftTime);
        this.timeBoundRight = rightTime;
        this.timeBoundRightSec = TimeKit.convertSeconds(rightTime);
        this.timeBucket = String.valueOf(timeBoundRight);
        this.mqDispatcher = mqDispatcher;
        for (int i = 0; i < round; i++) {
            buckets[i] = new TimeBucket(i);
        }
    }

    /**
     * 服务初始化时，由于可能有大量MsgId要加载到内存，不可避免存在延迟消息滞后处理的问题
     * 故障恢复时也会调此方法初始化时间轮，但是会根据schedule_offset检索已投递的msg，仍然会有部分（10秒）已投递过msg重复投递
     */
    public void initialize(List<MsgItem> msgItems) {
        log.info("TimeBucketWheel initialize msgItems size={} {}", msgItems.size(), timeBoundRight);
        if (CollectionsKit.isEmpty(msgItems)) {
            return;
        }

        long nowSec = TimeKit.nowSeconds();
        for (MsgItem msgItem : msgItems) {
            Long deadlineSeconds = msgItem.getDeadlineSeconds();
            if (deadlineSeconds <= nowSec) {
                // 立即投递，这里应该交给MQDispatcher（异步）
                mqDispatcher.submit(timeBucket, msgItem.getId());
                continue;
            }
            buckets[((int) (deadlineSeconds - nowSec)) % round].add(msgItem);
        }
        log.info("TimeBucketWheel initialize done {}", timeBoundRight);
    }

    public void put(MsgItem msgItem) {
        log.info("TimeBucketWheel put msgItem={} {}", msgItem, timeBoundRight);

        Long deadlineSeconds = msgItem.getDeadlineSeconds();
        if (deadlineSeconds <= timeBoundLeftSec) {
            log.error("TimeBucketWheel msgItem deadline over equal timeBoundLeft={} id={}", timeBoundLeft, msgItem.getId());
            throw new BusinessException(ErrorCode.DELAYED_TIME_NOT_MATCH_WHEEL);
        }
        if (deadlineSeconds > timeBoundRightSec) {
            log.error("TimeBucketWheel msgItem deadline over timeBoundRight={} id={}", timeBoundRight, msgItem.getId());
            throw new BusinessException(ErrorCode.DELAYED_TIME_NOT_MATCH_WHEEL);
        }

        long nowSec = TimeKit.nowSeconds();
        if (deadlineSeconds <= nowSec) {
            // 立即投递，这里应该交给MQDispatcher（异步）
            mqDispatcher.submit(timeBucket, msgItem.getId());
            return;
        }
        /*
            先写入缓冲队列，可以避免：
            1.和RollingTimeService争抢tick读锁。
            2.万一发生较长时间停顿tick落后于真实时间，避免基于错误的tick计算错误的bucket下标，
                等到RollingTimeService恢复后计算出正确的tick放入
         */
        bufferQueue.add(msgItem);
    }

    /**
     * 拨动时间片
     */
    public void roll() {
        int count = rollBuckets();
        for (int i = 0; i < count; i++) {
            tick = (tick + 1) % round;

            if (log.isDebugEnabled()) {
                log.debug("TimeBucketWheel rolling now tick={}", tick);
            }

            List<Integer> deliverMsgIds = buckets[tick].trigger();
            if (CollectionsKit.isNotEmpty(deliverMsgIds)) {
                mqDispatcher.submit(timeBucket, deliverMsgIds);
            }

            if (log.isDebugEnabled()) {
                log.debug("TimeBucketWheel rolling done tick={}", tick);
            }
        }
        if (count > 0) {
            lastTriggerSec = TimeKit.nowSeconds();
        }
        transferMsg2Bucket();
    }

    private void transferMsg2Bucket() {
        int size = bufferQueue.size();
        if (size == 0) {
            return;
        }

        MsgItem msg ;
        long nowSec = TimeKit.nowSeconds();

        if (log.isDebugEnabled()) {
            log.debug("TimeBucketWheel transferMsg2Bucket size={} {}", size, timeBoundRight);
        }

        for (int i = 0; i < size; i++) {
            try {
                msg = bufferQueue.poll(0, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                log.error("TimeBucketWheel transferMsg2Bucket error", e);
                Thread.currentThread().interrupt();
                return;
            }
            if (null == msg) {
                // 不应该出现的情况
                log.error("TimeBucketWheel transferMsg2Bucket poll null but size={} > 0", size);
                return;
            }
            Long deadlineSeconds = msg.getDeadlineSeconds();
            if (deadlineSeconds <= nowSec) {
                // 立即投递，这里应该交给MQDispatcher（异步）
                mqDispatcher.submit(timeBucket, msg.getId());
                return;
            }
            buckets[(int) (tick + (deadlineSeconds - nowSec)) % round].add(msg);
        }
    }

    /**
     * 计算距离上次触发，过了多少秒，要处理多少个bucket
     */
    private int rollBuckets() {
        if (lastTriggerSec < 0) {
            // 当前时间轮刚初始化，需要立即执行一次
            return 1;
        }
        return (int) (TimeKit.nowSeconds() - lastTriggerSec);
    }

    public long getTimeBoundRight() {
        return timeBoundRight;
    }

    public long getTimeBoundRightSec() {
        return timeBoundRightSec;
    }

    public long getTimeBoundLeft() {
        return timeBoundLeft;
    }

    public long getTimeBoundLeftSec() {
        return timeBoundLeftSec;
    }

    public String getTimeBucket() {
        return timeBucket;
    }

    public void clear() {
        this.buckets = null;
        this.bufferQueue = null;
    }

}
