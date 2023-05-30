package com.mixc.cpms.schedule.mq.service.model.dto;

import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import lombok.Data;

import java.util.List;

/**
 * @author Joseph
 * @since 2023/1/29
 */
@Data
public class TimeSegmentDTO {

    private Long timeBoundLeft;
    private Long timeBoundRight;
    private List<DelayedMsg> delayedMsgList;


    public static TimeSegmentDTO build(Long timeBoundLeft, Long timeBoundRight, List<DelayedMsg> list) {
        TimeSegmentDTO dto = new TimeSegmentDTO();
        dto.setTimeBoundLeft(timeBoundLeft);
        dto.setTimeBoundRight(timeBoundRight);
        dto.setDelayedMsgList(list);
        return dto;
    }
}
