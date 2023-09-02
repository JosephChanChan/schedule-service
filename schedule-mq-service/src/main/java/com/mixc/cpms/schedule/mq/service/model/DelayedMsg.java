package com.mixc.cpms.schedule.mq.service.model;

import com.mixc.cpms.schedule.mq.client.dto.DelayedMsgDTO;
import com.mixc.cpms.schedule.mq.service.common.Constant;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * @author Joseph
 * @since 2023/1/18
 */
@Data
public class DelayedMsg {

    /*
        CREATE TABLE `时间片` (
          `id` int unsigned NOT NULL AUTO_INCREMENT,
          `schedule_service_code` varchar(36) NOT NULL,
          `deadline` datetime NOT NULL,
          `topic` varchar(50) not null,
          `tags` varchar(50) not null default '',
          `msg_content` text NOT NULL,
          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          PRIMARY KEY (`id`),
          KEY `IDX_SERVICE_CODE` (`schedule_service_code`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
     */
    private Integer id;

    /**
     * 消息调度服务编码
     */
    private String scheduleServiceCode;

    /**
     * 到期时间，投递
     */
    private Date deadline;

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

    private Date createTime;




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
        msgItem.setDeadlineSeconds(deadline.getTime() / Constant.ONE_SECOND);
        return msgItem;
    }

    public static DelayedMsg build(String code, DelayedMsgDTO dto) {
        DelayedMsg delayedMsg = new DelayedMsg();
        delayedMsg.setTopic(dto.getTopic());
        delayedMsg.setTags(dto.getTags());
        delayedMsg.setMsgContent(dto.getMsgContent());
        delayedMsg.setDeadline(dto.getExpectDeliverTime());
        delayedMsg.setScheduleServiceCode(code);
        delayedMsg.setCreateTime(new Date());
        return delayedMsg;
    }

}
