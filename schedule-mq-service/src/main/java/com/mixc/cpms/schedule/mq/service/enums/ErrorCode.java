package com.mixc.cpms.schedule.mq.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ErrorCode {

    UNKNOWN_ERROR(-1, "未知错误"),

    DELAYED_TIME_NOT_MATCH_WHEEL(100, "延迟消息时间匹配时间轮失败，请重试"),
    BUSINESS_ERROR(1000, "业务异常，请稍后再试"),
    ;


    private final int code;
    private final String msg;
}
