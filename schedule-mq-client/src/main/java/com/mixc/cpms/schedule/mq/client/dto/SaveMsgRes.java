package com.mixc.cpms.schedule.mq.client.dto;

import lombok.*;

/**
 * @author Joseph
 * @since 2023/5/30
 */
@Builder
@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveMsgRes {

    private Integer id;

    private String segment;
}
