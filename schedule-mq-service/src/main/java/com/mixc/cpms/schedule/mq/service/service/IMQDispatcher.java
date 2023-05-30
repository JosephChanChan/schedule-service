package com.mixc.cpms.schedule.mq.service.service;

import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import com.mixc.cpms.schedule.mq.service.model.MsgItem;

import java.util.List;

/**
 * @author Joseph
 * @since 2023/1/22
 */
public interface IMQDispatcher {

    void submit(String timeBucket, Long msgId);

    void submit(String timeBucket, List<Long> msgIds);

    void retryDeliver(DelayedMsg delayedMsg);


}
