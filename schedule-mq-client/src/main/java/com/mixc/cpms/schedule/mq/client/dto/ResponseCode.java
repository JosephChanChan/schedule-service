package com.mixc.cpms.schedule.mq.client.dto;

import lombok.Getter;

/**
 * @author Joseph
 * @since 2023/5/30
 */
@Getter
public enum ResponseCode {

    SUCCESS("SUCCESS", "请求成功"),
    UNKNOWN_ERROR("UNKNOWN_ERROR", "未知错误")
    ;

    String code;
    String msg;

    ResponseCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
