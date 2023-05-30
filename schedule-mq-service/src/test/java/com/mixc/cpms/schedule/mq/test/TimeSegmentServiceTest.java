package com.mixc.cpms.schedule.mq.test;

import com.mixc.cpms.schedule.mq.service.ScheduleMqServiceApplication;
import com.mixc.cpms.schedule.mq.client.dto.DelayedMsgDTO;
import com.mixc.cpms.schedule.mq.service.service.ITimeBucketService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;


/**
 * @author Joseph
 * @since 2023/1/18
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScheduleMqServiceApplication.class)
public class TimeSegmentServiceTest {

    @Autowired
    private ITimeBucketService timeSegmentService;


    @Test
    public void insert() {
        DelayedMsgDTO msg = new DelayedMsgDTO();
        msg.setScheduleServiceCode("xxxxx-2010");
        msg.setDeadline(new Date(System.currentTimeMillis() + 3600 * 24 * 3));
        msg.setMsgContent("test");
        long id = timeSegmentService.insert("", msg);
        log.info("timeSegment id={}", id);
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
