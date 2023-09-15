package com.mixc.cpms.schedule.mq.test;

import cn.hutool.core.date.DateUtil;
import com.mixc.cpms.schedule.mq.client.kit.StringKit;
import com.mixc.cpms.schedule.mq.service.ScheduleMqServiceApplication;
import com.mixc.cpms.schedule.mq.service.common.Constant;
import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import com.mixc.cpms.schedule.mq.service.model.dto.TimeSegmentDTO;
import com.mixc.cpms.schedule.mq.service.service.ITimeBucketService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.BadSqlGrammarException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


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
        msg.setCreateTime(new Date());
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

    /**
     * 随机大量插入延迟消息到一个时间片
     */
    @Test
    public void randomDeadlineMsgInTimeSegment() {
        int count = 10000;
        String topic = "TestA";
        String tag = "TagA";
        Date baseTime = DateUtil.parse("2023-09-15 10:36:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String segment = "202309151036";
        Date date = new Date();
        List<DelayedMsg> list = new ArrayList<>(count);
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            DelayedMsg msg = new DelayedMsg();
            msg.setTopic(topic);
            msg.setTags(tag);
            msg.setCreateTime(date);
            msg.setScheduleServiceCode("A");
            msg.setMsgContent(StringKit.randomString(50));
            msg.setDeadline(new Date(baseTime.getTime() + random.nextInt(1800000)));
            list.add(msg);
            if (list.size() % Constant.BATCH_READ_DELAYED_MSG_SIZE == 0) {
                timeSegmentService.batchInsertDelayedMsg(segment, list);
                list.clear();
            }
        }
    }

    /**
     * 从指定时间片的指定偏移ID开始读取全部消息
     */
    @Test
    public void readFromSegmentSpecificFromStartId() {
        String segment = "202309151036";
        long startId = 9522;
        TimeSegmentDTO timeSegmentDTO = timeSegmentService.loadSegmentFrom(segment, startId);
        if (null != timeSegmentDTO.getDelayedMsgList()) {
            System.out.println(timeSegmentDTO.getDelayedMsgList().size());
        }
    }

    /**
     * 读取下一个时间片消息
     */
    @Test
    public void readNextSegment() {
        TimeSegmentDTO timeSegmentDTO = timeSegmentService.loadNextSegment(202309151006L);
        if (null == timeSegmentDTO) {
            System.out.println("没有下一个时间片");
        }
        if (null != timeSegmentDTO && null != timeSegmentDTO.getDelayedMsgList()) {
            System.out.println(timeSegmentDTO.getDelayedMsgList().size());
        }
    }

    /**
     * 读取某范围内的消息
     */
    @Test
    public void readInIdRange() {
        TimeSegmentDTO timeSegmentDTO = timeSegmentService.loadSegmentInRange("202309151036", 10000L, 19000L);
        if (null != timeSegmentDTO && null != timeSegmentDTO.getDelayedMsgList()) {
            System.out.println(timeSegmentDTO.getDelayedMsgList().size());
        }
    }
}
