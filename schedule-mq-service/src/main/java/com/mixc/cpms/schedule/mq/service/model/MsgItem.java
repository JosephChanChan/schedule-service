package com.mixc.cpms.schedule.mq.service.model;

import com.mixc.cpms.schedule.mq.client.dto.DelayedMsgDTO;
import lombok.Data;

/**
 * @author Joseph
 * @since 2023/1/19
 */
@Data
public class MsgItem {

    /**
     * ID
     */
    private Integer id;

    /**
     * 到期执行时间，秒级
     */
    private Long deadlineSeconds;

    public static MsgItem build(int id, DelayedMsgDTO dto) {
        MsgItem msgItem = new MsgItem();
        msgItem.setId(id);
        msgItem.setDeadlineSeconds(dto.getDeadlineSeconds());
        return msgItem;
    }
}