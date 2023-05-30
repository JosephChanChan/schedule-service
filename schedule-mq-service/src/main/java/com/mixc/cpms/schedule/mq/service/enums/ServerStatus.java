package com.mixc.cpms.schedule.mq.service.enums;

import lombok.Getter;

/**
 * @author Joseph
 * @since 2023/2/6
 */
@Getter
public enum ServerStatus {

    /*
        INITIALING -> RUNNING -> CLOSING -> TERMINATED
     */

    INITIALING,
    RUNNING,
    CLOSING,
    TERMINATED
    ;
}
