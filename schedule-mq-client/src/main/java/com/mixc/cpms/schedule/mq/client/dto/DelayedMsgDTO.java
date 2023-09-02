package com.mixc.cpms.schedule.mq.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mixc.cpms.schedule.mq.client.kit.AssertKit;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;


/**
 * @author Joseph
 * @since 2023/1/29
 */
@Data
public class DelayedMsgDTO {

    private static final int ONE_SECOND_MILLIS = 1000;

    /**
     * 要投递的topic。不能为空
     */
    private String topic;

    /**
     * 要投递的Tag，英文逗号分开。可为空
     */
    private String tags;

    /**
     * 消息体内容，此内容会完整的被写到 #{@link org.apache.rocketmq.common.message.Message#body}
     * 如果服务收到请求的时间 >= expectDeliverTime，会立即投递消息
     * 不能为空
     */
    private String msgContent;

    /**
     * 消息期望投递的时间。
     * 当服务收到消息的时候 >= expectDeliverTime，消息会被立即投递
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expectDeliverTime;



    public void checkParams() {
        AssertKit.notBlank(topic, "消息Topic不能为空");
        AssertKit.notBlank(msgContent, "消息体不能为空");
        AssertKit.notNull(expectDeliverTime, "期望投递时间不能为空");
    }

    public Long getDeadlineSeconds() {
        return expectDeliverTime.getTime() / ONE_SECOND_MILLIS;
    }


}
