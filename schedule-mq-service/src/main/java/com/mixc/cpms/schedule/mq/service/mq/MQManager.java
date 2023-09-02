package com.mixc.cpms.schedule.mq.service.mq;

import com.mixc.cpms.schedule.mq.client.kit.StringKit;
import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Joseph
 * @since 2023/1/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MQManager {

    private final RocketMQTemplate mqTemplate;


    public void syncSend(DelayedMsg msg) {
        Integer id = msg.getId();
        String topic = msg.getTopic();
        String tags = msg.getTags();
        String msgContent = msg.getMsgContent();
        if (StringKit.notBlank(tags)) {
            topic = topic.concat(":").concat(tags);
        }
        // 只要producer没抛错，msg至少被写入broker的OS内存中，除非对msg可靠性要求极高场景，需要判断是否落盘
        SendResult sendResult = mqTemplate.syncSend(topic, msgContent);
        String msgId = sendResult.getMsgId();
        SendStatus sendStatus = sendResult.getSendStatus();
        log.info("MQManager syncSend delayedMsg id={} msgId={} sendStatus={}", id, msgId, sendStatus);
    }

    public void syncBatchSend(List<DelayedMsg> msgList) {
        msgList.forEach(this::syncSend);
    }



}
