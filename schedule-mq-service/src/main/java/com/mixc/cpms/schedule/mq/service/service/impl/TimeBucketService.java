package com.mixc.cpms.schedule.mq.service.service.impl;

import cn.hutool.core.util.IdUtil;
import com.mixc.cpms.schedule.mq.client.kit.NumberKit;
import com.mixc.cpms.schedule.mq.service.common.*;
import com.mixc.cpms.schedule.mq.service.config.ApplicationConfig;
import com.mixc.cpms.schedule.mq.service.config.SpringCoordinator;
import com.mixc.cpms.schedule.mq.service.dao.ITimeBucketMapper;
import com.mixc.cpms.schedule.mq.service.exception.BusinessException;
import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import com.mixc.cpms.schedule.mq.client.dto.DelayedMsgDTO;
import com.mixc.cpms.schedule.mq.service.model.dto.LimitDTO;
import com.mixc.cpms.schedule.mq.client.dto.SaveMsgRes;
import com.mixc.cpms.schedule.mq.service.model.dto.TimeSegmentDTO;
import com.mixc.cpms.schedule.mq.service.service.IDistributionLockService;
import com.mixc.cpms.schedule.mq.service.service.ITimeBucketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author Joseph
 * @since 2023/1/18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeBucketService implements ITimeBucketService {


    private final ApplicationConfig applicationConfig;

    private final ITimeBucketMapper timeBucketMapper;

    private final IDistributionLockService distributionLockService;

    private final SpringCoordinator springCoordinator;



    @Override
    public List<Long> showAllSegments() {
        List<String> list = timeBucketMapper.showAllTables();
        if (CollectionsKit.isEmpty(list)) {
            return null;
        }
        List<Long> ans = new ArrayList<>(list.size());
        for (String elm : list) {
            try {
                ans.add(Long.parseLong(elm));
            }
            catch (NumberFormatException e) {
                // ignoring
            }
        }
        ans.sort((o1, o2) -> (int) (o1-o2));
        return ans;
    }

    @Override
    public void initializeSegments() {
        /*
            case1：没有时间片，覆盖范围是当前时间 + k天 <= 30天，按每30分钟创建一个时间片
            case2：有时间片，最大的时间片 < 当前时间，按照case1创建
            case3：有时间片，最大的时间片 >= 当前时间，覆盖范围是 max{segment} + k天 <= 30天，按每30分钟创建一个时间片
         */
        Long nowTime = Long.parseLong(LocalDateTime.now().format(DateTimeFormat.yyyyMMddHHmm));
        Long segment = nowTime;

        List<Long> segments = showAllSegments();

        if (CollectionsKit.isNotEmpty(segments)) {
            Long maxSegment = segments.stream().max(Comparator.naturalOrder()).orElse(segment);
            if (maxSegment >= segment) {
                segment = maxSegment;
            }
        }
        // 当前时间片的秒数
        long segmentSeconds = TimeKit.convertSeconds(segment);
        // 从当前时间起创建30天时间片的边界
        long maxLimitSeconds = TimeKit.convertSeconds(nowTime) + Constant.SECONDS_OF_30_DAYS;
        long intervalSeconds = Constant.SECONDS_OF_30_MINUTES;

        String lockOwner = IdUtil.fastSimpleUUID();
        distributionLockService.lock(Constant.CREATE_NEW_SEGMENT_DIS_LOCK, lockOwner, 10);

        try {
            ITimeBucketService self = springCoordinator.getBean(ITimeBucketService.class);

            // 从segment起，每30分钟创建一个时间片，直到 > maxLimitSeconds
            while (segmentSeconds + intervalSeconds <= maxLimitSeconds) {
                String newSegment = TimeKit.plus30Minutes(segment);

                self.createNewSegment(newSegment);

                segment = Long.parseLong(newSegment);
                segmentSeconds = TimeKit.convertSeconds(segment);
            }
        }
        finally {
            distributionLockService.unlock(Constant.CREATE_NEW_SEGMENT_DIS_LOCK, lockOwner);
        }
    }

    @Override
    public long insert(String tableName, DelayedMsg delayedMsg) {
        log.info("TimeBucket insert new {} delayedMsg={}", tableName, delayedMsg);
        return timeBucketMapper.insert(tableName, delayedMsg);
    }

    @Override
    public SaveMsgRes putDelayedMsg(String serviceCode, DelayedMsgDTO dto) {
        log.info("TimeBucket putDelayedMsg serviceCode={} dto={}", serviceCode, dto);

        Long deadlineSeconds = dto.getDeadlineSeconds();
        Date expectDeliverTime = dto.getExpectDeliverTime();
        long deadlineSegment = TimeKit.convertSegmentStyle(expectDeliverTime);
        long limitTime = TimeKit.nowSeconds() + Constant.SECONDS_OF_30_DAYS;
        if (deadlineSeconds > limitTime) {
            log.error("TimeBucket putDelayedMsg delayed time over limitTime {} {}", deadlineSeconds, limitTime);
            throw new BusinessException("延迟消息最大延迟时间不能超过30天");
        }

        List<Long> segments = showAllSegments();
        // 找到能cover到期时间的时间片
        int minIdx = CollectionsKit.binarySearchFloor(deadlineSegment, segments, true);
        Long segment = segments.get(minIdx);

        // 消息落库
        String segmentName = String.valueOf(segment);
        DelayedMsg model = DelayedMsg.build(applicationConfig.getScheduleServiceCode(), dto);
        insert(segmentName, model);
        Long id = model.getId();
        log.info("TimeBucket putDelayedMsg done id={}", id);

        return SaveMsgRes.builder().id(id).segment(segmentName).build();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void createNewSegment(String newSegmentName) {
        log.info("TimeBucket createNewSegment timeSegment={}", newSegmentName);
        timeBucketMapper.createNewSegment(newSegmentName);
        log.info("TimeBucket createNewSegment success");
    }

    @Override
    public List<DelayedMsg> getMsgContents(String tableName, List<Long> ids) {
        // 这里会打出大量id，后面屏蔽掉
        log.info("TimeBucket getMsgContents time={} ids={}", tableName, ids);
        return timeBucketMapper.getMsgContent(tableName, ids);
    }

    @Override
    public TimeSegmentDTO loadNextSegment(Long timeStart) {
        List<Long> timeSegments = showAllSegments();

        // 下一个时间片min{i} > timeStart
        int i = CollectionsKit.binarySearchFloor(timeStart, timeSegments, false);
        if (NumberKit.lt0(i)) {
            log.info("TimeBucket loadNextSegment no segment after timeStart={} i={}", timeStart, i);
            return null;
        }

        String nextSegment = String.valueOf(timeSegments.get(i));
        log.info("TimeBucket loadNextSegment timeStart={} nextSegment={}", timeStart, nextSegment);

        String scheduleServiceCode = applicationConfig.getScheduleServiceCode();
        Integer count = timeBucketMapper.countSegmentMsg(nextSegment, scheduleServiceCode, null);
        LimitDTO limitDTO = new LimitDTO();
        limitDTO.setSize(Constant.BATCH_READ_DELAYED_MSG_SIZE);

        List<DelayedMsg> delayedMsgList = new ArrayList<>(count);
        log.info("TimeBucket loadNextSegment={} expect msgCount={} loadSize={}",
                nextSegment, count, Constant.BATCH_READ_DELAYED_MSG_SIZE);

        int batchSize = Constant.BATCH_READ_DELAYED_MSG_SIZE;
        BatchOperationKit.batchExecute(count, batchSize, start -> {
            limitDTO.setStart(start);
            List<DelayedMsg> pageResult = timeBucketMapper.getMsgIndexByPage(nextSegment, scheduleServiceCode, limitDTO);
            delayedMsgList.addAll(pageResult);
        });
        log.info("TimeBucket loadNextSegment={} realMsgCount={}", nextSegment, delayedMsgList.size());

        TimeSegmentDTO dto = new TimeSegmentDTO();
        dto.setTimeBoundLeft(timeStart);
        dto.setTimeBoundRight(timeSegments.get(i));
        dto.setDelayedMsgList(delayedMsgList);
        return dto;
    }

    @Override
    public TimeSegmentDTO loadSegmentInRange(String specificSegment, Long fromId, Long toId) {
        LimitDTO limitDTO = new LimitDTO();
        limitDTO.setFromId(fromId);
        limitDTO.setToId(toId);
        List<DelayedMsg> list = timeBucketMapper.getMsgIndexInRange(
                specificSegment, applicationConfig.getScheduleServiceCode(), limitDTO);
        TimeSegmentDTO dto = new TimeSegmentDTO();
        dto.setDelayedMsgList(list);
        return dto;
    }

    @Override
    public TimeSegmentDTO loadSegmentFrom(String specificSegment, Long startId) {
        log.info("TimeBucket loadSegmentFrom={} startId={}", specificSegment, startId);

        int batchSize = Constant.BATCH_READ_DELAYED_MSG_SIZE;

        LimitDTO limitDTO = new LimitDTO();
        limitDTO.setFromId(startId);
        String scheduleServiceCode = applicationConfig.getScheduleServiceCode();
        Integer count = timeBucketMapper.countSegmentMsg(specificSegment, scheduleServiceCode, limitDTO);

        if (NumberKit.lte0(count)) {
            log.info("TimeBucket loadSegmentFrom={} startId={} have no msg", specificSegment, startId);
            return TimeSegmentDTO.build(null, Long.parseLong(specificSegment), null);
        }

        List<DelayedMsg> delayedMsgList = new ArrayList<>(count);
        log.info("TimeBucket loadSegmentFrom={} startId={} expect msgCount={} loadSize={}", specificSegment, startId, count, batchSize);

        limitDTO.setSize(batchSize);
        BatchOperationKit.batchExecute(count, batchSize, start -> {

            limitDTO.setFromId(delayedMsgList.size() == 0 ? startId : delayedMsgList.get(delayedMsgList.size()-1).getId());
            delayedMsgList.addAll(timeBucketMapper.getMsgIndexFrom(specificSegment, scheduleServiceCode, limitDTO));
        });

        log.info("TimeBucket loadSegmentFrom done {} startId={} actualCount={}", specificSegment, startId, delayedMsgList.size());

        return TimeSegmentDTO.build(null, Long.parseLong(specificSegment), delayedMsgList);
    }

    @Override
    public Long maxIdFromSegment(String tableName) {
        return timeBucketMapper.maxIdFromSegment(tableName, applicationConfig.getScheduleServiceCode());
    }
}
