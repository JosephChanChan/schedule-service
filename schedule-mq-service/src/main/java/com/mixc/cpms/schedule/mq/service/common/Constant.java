package com.mixc.cpms.schedule.mq.service.common;

/**
 * @author Joseph
 * @since 2022/5/8
 */
public class Constant {

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
     * 最大支持消息的延迟投递天数
     */
    public static final int MAX_DELAYED_DAYS = 30;

    /**
     * 30天的秒数
     */
    public static final long SECONDS_OF_30_DAYS = 3600 * 24 * 30;

    public static final long SECONDS_OF_30_MINUTES = 1800;

    /**
     * 创建时间片的锁名
     */
    public static final String CREATE_NEW_SEGMENT_DIS_LOCK = "CREATE_NEW_SEGMENT_DIS_LOCK";

    /**
     * 竞争分布式锁失败后暂停时间
     */
    public static final long THREAD_RACE_LOCK_INTERVAL_MILLIS = 10;

}
