package com.mixc.cpms.schedule.mq.service.common;


/**
 * @author Joseph
 * @since 2023/6/17
 */
public class ThreadKit {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
