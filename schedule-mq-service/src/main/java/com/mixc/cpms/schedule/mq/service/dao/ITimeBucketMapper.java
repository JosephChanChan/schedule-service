package com.mixc.cpms.schedule.mq.service.dao;

import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import com.mixc.cpms.schedule.mq.client.dto.DelayedMsgDTO;
import com.mixc.cpms.schedule.mq.service.model.dto.LimitDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author Joseph
 * @since 2023/1/18
 */
@Mapper
public interface ITimeBucketMapper {


    List<String> showAllTables();

    void createNewSegment(@Param("tableName") String tableName);

    long insert(@Param("tableName") String tableName, @Param("delayedMsg") DelayedMsg delayedMsg);

    List<DelayedMsg> getMsgContent(@Param("tableName") String tableName, @Param("ids") List<Integer> ids);

    List<DelayedMsg> getMsgIndexByPage(@Param("tableName") String tableName,
                                       @Param("scheduleServiceCode") String scheduleServiceCode,
                                       @Param("limitDTO") LimitDTO limitDTO);

    List<DelayedMsg> getMsgIndexInRange(@Param("tableName") String tableName,
                                       @Param("scheduleServiceCode") String scheduleServiceCode,
                                       @Param("limitDTO") LimitDTO limitDTO);

    List<DelayedMsg> getMsgIndexFrom(@Param("tableName") String tableName,
                                     @Param("scheduleServiceCode") String scheduleServiceCode,
                                     @Param("limitDTO") LimitDTO limitDTO);

    Integer countSegmentMsg(@Param("tableName") String tableName,
                            @Param("scheduleServiceCode") String scheduleServiceCode,
                            @Param("limitDTO") LimitDTO limitDTO);

    Long maxIdFromSegment(@Param("tableName") String tableName, @Param("scheduleServiceCode") String scheduleServiceCode);


}
