package com.mixc.cpms.schedule.mq.service.listener;

import com.mixc.cpms.schedule.mq.service.controller.ScheduleController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

/**
 * @author Joseph
 * @since 2023/2/6
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationInitialListener implements ApplicationRunner {

    private final ScheduleController scheduleController ;

    @Override
    public void run(ApplicationArguments args) {
        log.info("[ScheduleService] Server Initialing!");

        scheduleController.initialize();

        // 执行清理工作
        Runtime.getRuntime().addShutdownHook(new Thread(scheduleController::stopServer));

        log.info("[ScheduleService] Server Initialize done!");
    }











}
