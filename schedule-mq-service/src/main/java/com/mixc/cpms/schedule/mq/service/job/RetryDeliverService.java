package com.mixc.cpms.schedule.mq.service.job;

import com.mixc.cpms.schedule.mq.service.cache.MsgDeliverInfoHolder;
import com.mixc.cpms.schedule.mq.service.common.CollectionsKit;
import com.mixc.cpms.schedule.mq.service.common.Constant;
import com.mixc.cpms.schedule.mq.service.common.ThreadFactoryImpl;
import com.mixc.cpms.schedule.mq.service.common.TimeKit;
import com.mixc.cpms.schedule.mq.service.controller.ScheduleController;
import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import com.mixc.cpms.schedule.mq.service.service.IMQDispatcher;
import com.mixc.cpms.schedule.mq.service.service.impl.MQDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.net.ServerSocket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 重新投递DelayedMsg的线程
 *
 * @author Joseph
 * @since 2023/1/25
 */
@Slf4j
public class RetryDeliverService {

    private final ScheduledExecutorService driver =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("RetryDeliverThread"));

    private IMQDispatcher mqDispatcher;

    private MsgDeliverInfoHolder msgDeliverInfoHolder;


    public void start(MsgDeliverInfoHolder holder, IMQDispatcher mqDispatcher) {
        this.msgDeliverInfoHolder = holder;
        this.mqDispatcher = mqDispatcher;

        log.info("RetryDeliverService beginning...");
        driver.scheduleAtFixedRate(new RetryDeliverJob(), 10000, 100, TimeUnit.MILLISECONDS);
        log.info("RetryDeliverService driver is on the way!");
    }

    public void stop() {
        driver.shutdown();
        // TODO 开异步线程把待重试的消息落盘
    }

    class RetryDeliverJob implements Runnable {

        @Override
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("RetryDeliverJob launching");
            }
            LinkedList<DelayedMsg>[] reads = msgDeliverInfoHolder.getReads();
            try {
                for (int i = 0; i < reads.length; i++) {
                    LinkedList<DelayedMsg> list = reads[i];
                    if (CollectionsKit.isEmpty(list)) {
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("RetryDeliverJob dealing queueIdx={} waiting count={}", i, list.size());
                    }
                    int redeliverCount = 0;
                    long nowMillis = TimeKit.nowMillis();
                    for (Iterator<DelayedMsg> it = list.iterator(); it.hasNext(); ) {

                        DelayedMsg delayedMsg = it.next();
                        int retryDelayedMillis = delayedMsg.getRetryDelayedMillis();
                        long lastFailDeliverTime = delayedMsg.getLastFailDeliverTime();

                        if ((nowMillis - lastFailDeliverTime) >= retryDelayedMillis) {
                            mqDispatcher.retryDeliver(delayedMsg);
                            it.remove();
                            redeliverCount++;
                            log.info("DelayedMsg redeliver timeBucket={} msgId={}", delayedMsg.getTimeBucket(), delayedMsg.getId());
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("RetryDeliverJob deal queue done idx={} redeliverCount={}", i, redeliverCount);
                    }
                }

                msgDeliverInfoHolder.swapDelayedMsgQueue();
                if (log.isDebugEnabled()) {
                    log.info("RetryDeliverJob done");
                }
            }
            catch (RuntimeException e) {
                log.error("RetryDeliverJob error!!!", e);
            }
        }
    }


}
