package com.mixc.cpms.schedule.mq.service.common;

import java.util.*;

/**
 * @author Joseph
 */
public class CollectionsKit {

    public static boolean isEmpty(Collection collection) {
        return null == collection || collection.isEmpty();
    }

    public static boolean isEmpty(Map map) {
        return null == map || map.isEmpty();
    }

    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }

    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }

    public static <T> List<T> arrayList(T... t) {
        if (null == t) {
            return new ArrayList<>(0);
        }
        return Arrays.asList(t);
    }

    public static <T> List<T> linkedList(T... t) {
        if (null == t) {
            return new LinkedList<>();
        }
        LinkedList<T> list = new LinkedList<>();
        for (T item : t) {
            list.addLast(item);
        }
        return list;
    }

    /**
     * 二分搜索目标值，返回目标值的下标，如果没有返回-1
     *
     * @param target 可比较的目标值
     * @param list 搜索空间
     * @return 目标值下标
     */
    public static <T extends Comparable<? super T>> int binarySearch(T target, List<T> list) {
        int l = 0, r = list.size()-1, m ;
        while (l + 1 < r) {
            m = ((r - l) >> 1) + l;
            T mid = list.get(m);
            if (target.compareTo(mid) == 0) {
                return m;
            }
            else if (target.compareTo(mid) > 0) {
                l = m;
            }
            else {
                r = m;
            }
        }
        if (target.compareTo(list.get(l)) == 0) {
            return l;
        }
        if (target.compareTo(list.get(r)) == 0) {
            return r;
        }
        return -1;
    }

    /**
     * 二分搜索大于等于目标值的最小值，返回其下标，如果没有返回-1
     *
     * @param target 目标值
     * @param list 搜索空间
     * @return 大于目标值的最小值的下标
     */
    public static <T extends Comparable<? super T>> int binarySearchFloor(T target, List<T> list, boolean equal) {
        if (CollectionsKit.isNotEmpty(list)) {
            int l = 0, r = list.size()-1, m ;
            while (l + 1 < r) {
                m = ((r - l) >> 1) + l;
                T mid = list.get(m);
                if (target.compareTo(mid) >= 0) {
                    l = m;
                }
                else {
                    r = m;
                }
            }
            if (equal ? list.get(l).compareTo(target) >= 0 : list.get(l).compareTo(target) > 0) {
                return l;
            }
            if (equal ? list.get(r).compareTo(target) >= 0 : list.get(r).compareTo(target) > 0) {
                return r;
            }
        }
        return -1;
    }

    /**
     * 二分搜索小于目标值的最大值，返回其下标，如果没有返回-1
     *
     * @param target 目标值
     * @param list 搜索空间
     * @return 小于目标值的最大值的下标
     */
    public static <T extends Comparable<? super T>> int binarySearchCeiling(T target, List<T> list) {
        if (CollectionsKit.isNotEmpty(list)) {
            int l = 0, r = list.size()-1, m ;
            while (l + 1 < r) {
                m = ((r - l) >> 1) + l;
                T mid = list.get(m);
                if (target.compareTo(mid) > 0) {
                    l = m;
                }
                else {
                    r = m;
                }
            }
            if (list.get(r).compareTo(target) < 0) {
                return r;
            }
            if (list.get(l).compareTo(target) < 0) {
                return l;
            }
        }
        return -1;
    }

}