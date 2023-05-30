package com.mixc.cpms.schedule.mq.service.common;

import java.time.format.DateTimeFormatter;

/**
 * @author Joseph
 * @since 2023/5/24
 */
public class DateTimeFormat {

    public static final DateTimeFormatter yyyyMMddHHmm = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public static final DateTimeFormatter yyyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
}
