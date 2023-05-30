package com.mixc.cpms.schedule.mq.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "com.mixc.cpms.schedule.mq.service")
public class ScheduleMqServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleMqServiceApplication.class, args);
    }

}
