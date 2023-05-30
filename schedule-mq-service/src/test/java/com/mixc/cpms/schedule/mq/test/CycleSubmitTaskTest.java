package com.mixc.cpms.schedule.mq.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Joseph
 * @since 2023/5/24
 */
@Slf4j
public class CycleSubmitTaskTest {

    private static final ThreadPoolTaskExecutor EXECUTOR;

    static {
        EXECUTOR = myExecutor();
    }

    /** 初始化线程池 */
    public static ThreadPoolTaskExecutor myExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(2);
        // 最大线程数
        executor.setMaxPoolSize(4);
        // 排队任务队列
        executor.setQueueCapacity(5);
        // 线程名称前缀
        executor.setThreadNamePrefix("异步线程-");
        // 队列满后拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        // 线程最大回收时间
        executor.setKeepAliveSeconds(100);
        // 初始化线程
        executor.initialize();
        return executor;
    }

    /** 模拟测试 */
    public static void main(String[] args) throws Exception {
        // 主任务数量
        int mainJobNum = 20;

        CountDownLatch mainDownLatch = new CountDownLatch(mainJobNum);

        for (int i = 0; i < mainJobNum; i++) {
            // 主任务编号, 方便区分
            int index = i + 1;

            // 模拟每1秒开始一个主任务
            TimeUnit.SECONDS.sleep(1);

            EXECUTOR.submit(() -> {
                try {
                    log.debug("\t执行主任务" + index);

                    // 每个主任务随机包含N个子任务, 再异步调用线程池资源处理
                    int subJobNum = RandomUtils.nextInt(2, 3);
                    subJobWorkAsync(subJobNum, index);
                } finally {
                    mainDownLatch.countDown();
                }
            });
        }

        mainDownLatch.await();
        EXECUTOR.shutdown();
        log.info("完成所有任务 > > >");
    }

    /** 异步执行子任务 */
    private static void subJobWorkAsync(int subJobNum, int index) {
        CountDownLatch subDownLatch = new CountDownLatch(subJobNum);
        for (int j = 0; j < subJobNum; j++) {
            EXECUTOR.submit(() -> {
                try {
                    log.warn("\t\t\t执行一个" + index + "的子任务");
                    // 每个子任务模拟耗时
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    subDownLatch.countDown();
                }
            });
        }

        try {
            subDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
