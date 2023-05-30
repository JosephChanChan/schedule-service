package com.mixc.cpms.schedule.mq.service.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Joseph
 * @since 2023/1/29
 */
@Slf4j
@Getter
@Setter
@Configuration
public class ApplicationConfig {

    @Value("${schedule.serviceCode}")
    private String scheduleServiceCode;

    @Value("${schedule.distribution.lock.validSeconds}")
    private Integer distributionLockValidSec;


}
