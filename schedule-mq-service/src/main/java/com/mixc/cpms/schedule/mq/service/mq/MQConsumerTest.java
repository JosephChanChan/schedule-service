package com.mixc.cpms.schedule.mq.service.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author Joseph
 * @since 2023/1/23
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "testConsumer", topic = "testTopic")
public class MQConsumerTest implements RocketMQListener<String> {


    @Override
    public void onMessage(String message) {
        log.info("MQConsumerTest receive message="+message);
    }
}
