package com.mixc.cpms.schedule.mq.test;

import com.mixc.cpms.schedule.mq.service.mq.MQManager;
import com.mixc.cpms.schedule.mq.service.ScheduleMqServiceApplication;
import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Joseph
 * @since 2023/1/23
 */
@Slf4j
@SpringBootTest(classes = ScheduleMqServiceApplication.class)
public class MQManagerTest {

    @Autowired
    private MQManager mqManager;

    @Test
    public void sendMsg() {
        DelayedMsg msg = new DelayedMsg();
        msg.setId(123L);
        msg.setTopic("testTopic");
        msg.setTags("test");
        msg.setScheduleServiceCode("A");
        msg.setMsgContent("msg test");
        mqManager.syncSend(msg);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }























}
