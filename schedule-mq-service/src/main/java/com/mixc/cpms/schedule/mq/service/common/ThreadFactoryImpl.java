package com.mixc.cpms.schedule.mq.service.common;

import lombok.Getter;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Joseph
 * @since 2023/1/20
 */
public class ThreadFactoryImpl implements ThreadFactory {

    private final AtomicInteger threadIndex = new AtomicInteger(0);
    private final String threadNamePrefix;
    private final boolean daemon;

    public ThreadFactoryImpl(final String threadNamePrefix) {
        this(threadNamePrefix, false);
    }

    public ThreadFactoryImpl(final String threadNamePrefix, boolean daemon) {
        this.threadNamePrefix = threadNamePrefix;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new AdvisedThread(this.threadIndex.incrementAndGet(), threadNamePrefix, r);
        thread.setDaemon(daemon);
        return thread;
    }

    public static class AdvisedThread extends Thread {
        private final int threadIndex;
        private final String namePrefix;

        public AdvisedThread(int idx, String prefix, Runnable task) {
            super(task, prefix + idx);
            this.threadIndex = idx;
            this.namePrefix = prefix;
        }

        public int getThreadIndex() {
            return threadIndex;
        }
        public String getNamePrefix() {
            return namePrefix;
        }
    }
}
