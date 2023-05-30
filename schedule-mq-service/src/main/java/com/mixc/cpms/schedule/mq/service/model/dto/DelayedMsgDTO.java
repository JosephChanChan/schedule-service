package com.mixc.cpms.schedule.mq.service.model.dto;

import com.mixc.cpms.schedule.mq.service.common.AssertKit;
import com.mixc.cpms.schedule.mq.service.model.MsgItem;
import lombok.Data;

import java.util.Date;

/**
 * @author Joseph
 * @since 2023/1/29
 */
@Data
public class DelayedMsgDTO {

    /**
     * 要投递的topic
     */
    private String topic;

    /**
     * 要投递的Tag
     */
    private String tags;

    /**
     * 消息体内容，此内容会完整的被写到#{@link org.apache.rocketmq.common.message.Message#body}
     */
    private String msgContent;

    /**
     * 期望投递的到期时间
     */
    private Date deadline;

    private String scheduleServiceCode;


    public Long getDeadlineSeconds() {
        AssertKit.notNull(deadline, "期望投递的到期时间不能为空");
        return deadline.getTime() / 1000;
    }

    public MsgItem convertSimpleMsgDTO(Long id) {
        MsgItem msgItem = new MsgItem();
        msgItem.setId(id);
        msgItem.setDeadlineSeconds(getDeadlineSeconds());
        return msgItem;
    }
}
