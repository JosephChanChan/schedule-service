package com.mixc.cpms.schedule.mq.service.model;

import com.mixc.cpms.schedule.mq.service.cache.TimeBucketWheel;
import com.mixc.cpms.schedule.mq.service.common.CollectionsKit;
import com.mixc.cpms.schedule.mq.service.common.TimeKit;
import com.mixc.cpms.schedule.mq.service.service.IMQDispatcher;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Joseph
 * @since 2023/1/19
 */
@Data
@Slf4j
public class TimeBucket {

    /**
     * 滴答
     */
    private final int tick ;

    /**
     * 时间槽队列
     */
    private List<MsgItem> list = new LinkedList<>();


    public TimeBucket(int tick) {
        this.tick = tick;
    }

    public void add(MsgItem msgItem) {
        list.add(msgItem);
    }

    public void batchAdd(List<MsgItem> msgItemList) {
        log.info("TimeSegment batchAdd msg list size={}", msgItemList.size());
        if (CollectionsKit.isEmpty(msgItemList)) {
            return;
        }
        msgItemList.forEach(this::add);
    }

    public void initializeAdd(List<MsgItem> msgItemList) {
        log.info("TimeSegment initializeAdd msg list size={}", msgItemList.size());
        if (CollectionsKit.isEmpty(msgItemList)) {
            return;
        }
        list.addAll(msgItemList);
    }

    public List<Long> trigger() {
        long nowSec = TimeKit.nowSeconds();

        int size = list.size(), i = 0;

        if (log.isDebugEnabled()) {
            log.debug("TimeSegment being trigger tick={} size={} nowSec={}", tick, size, nowSec);
        }

        List<Long> deliverList = new ArrayList<>(32);

        for (Iterator<MsgItem> iterator = list.iterator(); iterator.hasNext() && i <= size; i++) {

            MsgItem msg = iterator.next();
            Long deadlineSeconds = msg.getDeadlineSeconds();

            if (nowSec < deadlineSeconds) {
                continue;
            }

            deliverList.add(msg.getId());
            iterator.remove();
        }
        if (log.isDebugEnabled()) {
            log.debug("TimeSegment trigger over tick={} nowSec={} deliverList={}", tick, nowSec, deliverList.size());
        }
        return deliverList;
    }
}
