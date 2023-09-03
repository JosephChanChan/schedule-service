package com.mixc.cpms.schedule.mq.service.cache;

import com.mixc.cpms.schedule.mq.service.common.Constant;
import com.mixc.cpms.schedule.mq.service.common.ThreadFactoryImpl;
import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedList;

/**
 * @author Joseph
 * @since 2023/1/25
 */
@Slf4j
@Component
public class MsgDeliverInfoHolder {

    /**
     * 重新尝试投递的递增延迟时间，毫秒
     */
    private final Integer[] retryDeliverMillis = new Integer[]{
            0, 200, 1000, 10000, 30000, 60000, 600000
    };

    /**
     * 重试投递的最大次数
     */
    private final byte maxRetryDeliverCount = (byte) retryDeliverMillis.length;

    /**
     * 按照线程号暂存，避免锁竞争
     */
    private volatile LinkedList<DelayedMsg>[] writes = new LinkedList[Constant.MQ_DISPATCHER_CORE_THREADS];
    /**
     * 读写数组
     * 在RetryDeliverService唤醒执行时，避免与投递线程竞争，交换读写数组
     */
    private volatile LinkedList<DelayedMsg>[] reads = new LinkedList[Constant.MQ_DISPATCHER_CORE_THREADS];


    public void initialize() {
        for (int i = 0; i < writes.length; i++) {
            writes[i] = new LinkedList<>();
            reads[i] = new LinkedList<>();
        }
    }

    public void postponeRetry(DelayedMsg delayedMsg) {
        Thread thread = Thread.currentThread();
        if (!(thread instanceof ThreadFactoryImpl.AdvisedThread)) {
            return;
        }
        ThreadFactoryImpl.AdvisedThread advisedThread = (ThreadFactoryImpl.AdvisedThread) thread;
        int threadIndex = advisedThread.getThreadIndex();

        delayedMsg.incrRetryDeliverCount();
        byte count = delayedMsg.getRetryDeliverCount();
        if (count > maxRetryDeliverCount-1) {
            // todo 告警
            log.error("DelayedMsg retry deliver reach max times! timeBucket={} msgId={}",
                    delayedMsg.getTimeBucket(), delayedMsg.getId());
            return;
        }
        int retryDeliverMillis = getRetryDeliverMillis(count);
        delayedMsg.setRetryDelayedMillis(retryDeliverMillis);
        delayedMsg.setLastFailDeliverTime(System.currentTimeMillis());

        writes[threadIndex].addLast(delayedMsg);
        log.info("AdvisedThread retry deliver till millis={} id={}", retryDeliverMillis, delayedMsg.getId());
    }

    public int getRetryDeliverMillis(byte count) {
        return retryDeliverMillis[count];
    }

    public void swapDelayedMsgQueue() {
        LinkedList<DelayedMsg>[] temp = writes;
        writes = reads;
        reads = temp;
    }

    public LinkedList<DelayedMsg>[] getReads() {
        return reads;
    }
}
