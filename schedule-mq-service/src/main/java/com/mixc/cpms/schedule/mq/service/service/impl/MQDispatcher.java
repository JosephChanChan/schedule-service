package com.mixc.cpms.schedule.mq.service.service.impl;

import com.mixc.cpms.schedule.mq.client.kit.StringKit;
import com.mixc.cpms.schedule.mq.service.cache.MsgDeliverInfoHolder;
import com.mixc.cpms.schedule.mq.service.cache.ScheduleOffsetHolder;
import com.mixc.cpms.schedule.mq.service.common.*;
import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import com.mixc.cpms.schedule.mq.service.mq.MQManager;
import com.mixc.cpms.schedule.mq.service.service.IMQDispatcher;
import com.mixc.cpms.schedule.mq.service.service.ITimeBucketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Joseph
 * @since 2023/1/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MQDispatcher implements IMQDispatcher {

    private final MQManager mqManager;

    private final ScheduleOffsetHolder offsetHolder;

    private final ITimeBucketService timeBucketService;

    private final MsgDeliverInfoHolder msgDeliverInfoHolder;

    /**
     * 默认的拒绝策略
     */
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            Constant.MQ_DISPATCHER_CORE_THREADS,
            Constant.MQ_DISPATCHER_CORE_THREADS,
            0,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryImpl("MQDispatcherThread"));


    @Override
    public void submit(String timeBucket, Integer msgId) {
        submit(timeBucket, CollectionsKit.arrayList(msgId));
    }

    @Override
    public void submit(String timeBucket, List<Integer> msgIds) {
        if (StringKit.blank(timeBucket) || CollectionsKit.isEmpty(msgIds)) {
            log.warn("MQDispatcher submit empty timeBucket={} msgIds={}", timeBucket, msgIds);
            return;
        }
        int size = msgIds.size();
        if (size <= Constant.MQ_DISPATCHER_BATCH_SIZE) {
            deliver(timeBucket, msgIds);
        }
        else {
            // 分页批量提交
            int batchSize = Constant.MQ_DISPATCHER_BATCH_SIZE;
            BatchOperationKit.batchExecute(size, batchSize, start -> {
                int end = Math.min(start + batchSize, size);
                List<Integer> subList = msgIds.subList(start, end);
                deliver(timeBucket, subList);
            });
        }
    }

    @Override
    public void retryDeliver(DelayedMsg delayedMsg) {
        executor.submit(() -> doDeliver(delayedMsg.getTimeBucket(), delayedMsg));
    }

    private void deliver(String timeBucket, List<Integer> msgIds) {
        executor.submit(() -> {
            List<DelayedMsg> delayedMsgList = timeBucketService.getMsgContents(timeBucket, msgIds);
            for (DelayedMsg delayedMsg : delayedMsgList) {
                executor.submit(() -> doDeliver(timeBucket, delayedMsg));
            }
        });
    }

    private void doDeliver(String timeBucket, DelayedMsg delayedMsg) {
        try {
            mqManager.syncSend(delayedMsg);
            updateDeliverOffset(timeBucket, delayedMsg.getId());
        }
        catch (RuntimeException e) {
            log.error("delayedMsg syncSend error timeBucket={} id={}", timeBucket, delayedMsg.getId());
            delayedMsg.setTimeBucket(timeBucket);
            // 未来优化，投递失败的msg先写入重试队列，由另外机制定时读出到内存进行递增重投
            msgDeliverInfoHolder.postponeRetry(delayedMsg);
        }
    }

    private void updateDeliverOffset(String timeBucket, Integer id) {
        offsetHolder.updateOffset(Long.parseLong(timeBucket), id);
    }
}
