package com.mixc.cpms.schedule.mq.service.service;

import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import com.mixc.cpms.schedule.mq.client.dto.DelayedMsgDTO;
import com.mixc.cpms.schedule.mq.client.dto.SaveMsgRes;
import com.mixc.cpms.schedule.mq.service.model.dto.TimeSegmentDTO;

import java.util.List;

/**
 * @author Joseph
 * @since 2023/1/18
 */
public interface ITimeBucketService {

    /**
     * 检索当前所有时间片
     *
     * @return 时间片
     */
    List<Long> showAllSegments();

    /**
     * 初始化时间片
     * case1：没有时间片，覆盖范围是当前时间 + k天 <= 30天，按每30分钟创建一个时间片
     * case2：有时间片，最大的时间片 < 当前时间，按照case1创建
     * case3：有时间片，最大的时间片 >= 当前时间，覆盖范围是 max{segment} + k天 <= 30天，按每30分钟创建一个时间片
     */
    void initializeSegments();

    /**
     * 写入延迟消息
     */
    long insert(String tableName, DelayedMsg delayedMsg);

    /**
     * 写入延迟消息
     *
     * @param serviceCode 消息所属服务编码
     * @param dto dto
     */
    SaveMsgRes putDelayedMsg(String serviceCode, DelayedMsgDTO dto);

    /**
     * 根据msgId检索消息内容
     */
    List<DelayedMsg> getMsgContents(String tableName, List<Integer> ids);

    /**
     * 加载时间片，从timeStart开始(不包括timeStart)的下一个最近时间片
     *
     * @param timeStart 起始时间，不包括
     * @return 包含延迟消息id和到期时间的list
     */
    TimeSegmentDTO loadNextSegment(Long timeStart);

    /**
     * 从指定时间片加载一个范围的延迟消息
     *
     * @param specificSegment 指定时间片
     * @param fromId 起始id，不包括
     * @param toId 结束id，包括
     * @return 包含延迟消息id和到期时间的list
     */
    TimeSegmentDTO loadSegmentInRange(String specificSegment, Long fromId, Long toId);

    /**
     * 从指定时间片和指定起始msg id开始加载消息
     *
     * @param specificSegment 指定时间片
     * @param startId 起始msg id，不包括
     * @return 包含延迟消息id和到期时间的list
     */
    TimeSegmentDTO loadSegmentFrom(String specificSegment, Long startId);

    Long maxIdFromSegment(String tableName);

    void createNewSegment(String newSegmentName);
}
