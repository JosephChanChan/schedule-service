package com.mixc.cpms.schedule.mq.test;

import com.mixc.cpms.schedule.mq.service.ScheduleMqServiceApplication;
import com.mixc.cpms.schedule.mq.client.dto.DelayedMsgDTO;
import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import com.mixc.cpms.schedule.mq.service.service.ITimeBucketService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


/**
 * @author Joseph
 * @since 2023/1/18
 */
@Slf4j
@SpringBootTest(classes = ScheduleMqServiceApplication.class)
public class TimeSegmentServiceTest {

    @Autowired
    private ITimeBucketService timeSegmentService;


    @Test
    public void insert() {
        DelayedMsg msg = new DelayedMsg();
        msg.setTopic("A");
        msg.setTags("B");
        msg.setScheduleServiceCode("AAA");
        msg.setCreateTime(LocalDateTime.now());
        msg.setMsgContent("test");
        msg.setDeadline(new Date());
        long c = timeSegmentService.insert("202307140834", msg);
        log.info("timeSegment c={}", c);
    }

    @Test
    public void showAllSegments() {
        List<Long> segments = timeSegmentService.showAllSegments();
        log.info("segments={}", segments);
    }

    @Test
    public void createRepeatSegment() {
        try {
            timeSegmentService.createNewSegment("202302262151");
        }
        catch (BadSqlGrammarException e) {
            log.warn("CheckTimeSegmentJob createNewSegment fail errorMsg={}", e.getMessage());
            if (e.getMessage().contains("already exists")) {
                log.warn("", e);
            }
        }

    }
}
