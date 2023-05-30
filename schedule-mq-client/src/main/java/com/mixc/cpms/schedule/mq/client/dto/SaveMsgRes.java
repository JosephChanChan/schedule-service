package com.mixc.cpms.schedule.mq.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Joseph
 * @since 2023/5/30
 */
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SaveMsgRes {

    public Long id;

    private String segment;
}
