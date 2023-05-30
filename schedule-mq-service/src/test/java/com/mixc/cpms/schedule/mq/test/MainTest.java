package com.mixc.cpms.schedule.mq.test;

import com.mixc.cpms.schedule.mq.service.common.CollectionsKit;
import com.mixc.cpms.schedule.mq.service.common.ThreadFactoryImpl;
import com.sun.media.jfxmediaimpl.HostUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Joseph
 * @since 2023/1/20
 */
public class MainTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public static void main(String[] args) {
        MainTest m = new MainTest();
        m.subListTest();
    }

    void subListTest() {
        List<Integer> list = CollectionsKit.linkedList(1, 2, 3, 4, 5);
        List<Integer> list2 = CollectionsKit.arrayList(1, 2, 3, 4, 5);
        List<Integer> sub = list.subList(1, 4);
        // LinkedList subList要定位到左边界花费O(n)时间
        Iterator<Integer> iterator = sub.iterator();
        for ( ; iterator.hasNext(); iterator.remove()) {
            Integer next = iterator.next();
            System.out.println(next);
        }
        sub = list2.subList(1, 4);
        iterator = sub.iterator();
        // ArrayList subList初始化和定位都是O(1)
        for ( ; iterator.hasNext(); /*iterator.remove() arraylist.subList不允许remove*/) {
            Integer next = iterator.next();
            System.out.println(next);
        }
    }

    void singleThreadExceptionTest() {
        ScheduledExecutorService driver =
                Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("TestA"));
        driver.scheduleAtFixedRate(() -> {
            System.out.println(System.currentTimeMillis());
            if (System.currentTimeMillis() % 30 == 0) {
                System.out.println("TestA error");
                // 抛出异常，driver线程会退出当前task，继续从blocking queue取任务
                throw new NullPointerException("TestA");
            }
        }, 1000, 100, TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Main exists");
    }

    void binarySearchTest() {
        List<Integer> a = CollectionsKit.arrayList(1, 3, 6, 10, 12, 20, 20, 20, 21, 30, 30, 41, 50);
        // 正常找个数
        System.out.println(CollectionsKit.binarySearch(20, a));// 6
        // 搜索不存在的数字
        System.out.println(CollectionsKit.binarySearch(7, a));// -1
        // 搜索不存在的右边界
        System.out.println(CollectionsKit.binarySearch(100, a));// -1
        // 搜索不存在的左边界
        System.out.println(CollectionsKit.binarySearch(-1, a));// -1

        // 正常搜索min > t
        System.out.println(CollectionsKit.binarySearchFloor(30, a, false));// 11
        System.out.println(CollectionsKit.binarySearchFloor(1, a, false));// 1
        System.out.println(CollectionsKit.binarySearchFloor(-1, a, false));// 0
        // 搜索不存在的min > t
        System.out.println(CollectionsKit.binarySearchFloor(50, a, false));// -1
        System.out.println(CollectionsKit.binarySearchFloor(100, a, false));// -1

        // 正常搜索max < t
        System.out.println(CollectionsKit.binarySearchCeiling(20, a));// 4
        System.out.println(CollectionsKit.binarySearchCeiling(50, a));// 11
        System.out.println(CollectionsKit.binarySearchCeiling(3, a));// 0
        System.out.println(CollectionsKit.binarySearchCeiling(100, a));// 12
        System.out.println(CollectionsKit.binarySearchCeiling(1, a));//
        // 搜索不存在的max < t
        System.out.println(CollectionsKit.binarySearchCeiling(1, a));// -1
        System.out.println(CollectionsKit.binarySearchCeiling(-1, a));// -1
    }

    void linkedListRemove() {
        List<Integer> list = new LinkedList<>();
        list.add(1);
        list.add(2);
        Iterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            Integer next = iterator.next();
            System.out.println(next);
            iterator.remove();
        }
    }

    void yyyyMMddHHmmTest() {
        long sec = LocalDateTime.parse("202301201722", FORMATTER)
                .toEpochSecond(ZoneOffset.of(ZoneOffset.systemDefault().getId()));
        long sec2 = LocalDateTime.parse("202301201725", FORMATTER)
                .toEpochSecond(ZoneOffset.of(ZoneOffset.systemDefault().getId()));
        System.out.println(sec);
        System.out.println(sec2);
        System.out.println(sec-sec2);
    }
}
