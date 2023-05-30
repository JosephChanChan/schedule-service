package com.mixc.cpms.schedule.mq.service.common;

/**
 * @author Joseph
 * @since 2022/5/8
 */
public class Constant {

    public static final String RANDOM_STRING =
            "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String NUM_STRING = "1234567890";

    /**
     * MQDispatcher线程数
     */
    public static final int MQ_DISPATCHER_CORE_THREADS = 32;

    /**
     * MQDispatcher批量查询延迟消息数
     */
    public static final int MQ_DISPATCHER_BATCH_SIZE = 1000;

    /**
     * 从时间片批量加载延迟消息数量
     */
    public static final int BATCH_READ_DELAYED_MSG_SIZE = 1000;

    /**
     * 预加载时间片的间隙时间，毫秒
     */
    public static final int CHECK_PRELOAD_TIME_SEGMENT_INTERVAL = 10000;

    /**
     * 30天的秒数
     */
    public static final long SECONDS_OF_30_DAYS = 3600 * 24 * 30;

    public static final long SECONDS_OF_30_MINUTES = 1800;

    /**
     * 竞争分布式锁失败后暂停时间
     */
    public static final long THREAD_RACE_LOCK_INTERVAL_MILLIS = 10;

}
