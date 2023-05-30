package com.mixc.cpms.schedule.mq.service.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Joseph
 * @since 2023/1/18
 */
@Data
public class DelayedMsg {

    /*
        create table `时间片`(
            `id` bigint UNSIGNED PRIMARY key auto_increment,
            `schedule_service_code` varchar(30) not null,
            `deadline` datetime not null,
            `msg_content` text not null
            `create_time` datetime not null default NOW()
        );
     */
    private Long id;

    /**
     * 消息调度服务编码
     */
    private String scheduleServiceCode;

    /**
     * 到期时间，投递
     */
    private LocalDateTime deadline;

    /**
     * msg topic
     */
    private String topic;

    /**
     * msg tags 英文逗号隔开
     */
    private String tags;

    /**
     * 消息体内容
     */
    private String msgContent;

    private LocalDateTime createTime;




    /* ---------------------------------------------- not table fields ---------------------------------------------  */

    private String timeBucket;

    /**
     * 重试投递的毫秒
     */
    private int retryDelayedMillis = 0;

    /**
     * 最近一次投递失败时间，毫秒
     */
    private long lastFailDeliverTime = 0;

    /**
     * 第几次重投递
     */
    private byte retryDeliverCount = 0;

    public byte incrRetryDeliverCount() {
        return retryDeliverCount++;
    }


    public MsgItem convert() {
        MsgItem msgItem = new MsgItem();
        msgItem.setId(id);
        msgItem.setDeadlineSeconds(deadline.toEpochSecond(ZoneOffset.of(ZoneOffset.systemDefault().getId())));
        return msgItem;
    }

}
