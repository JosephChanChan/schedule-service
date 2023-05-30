package com.mixc.cpms.schedule.mq.service.common;

import com.mixc.cpms.schedule.mq.service.exception.BusinessException;

import java.util.Collection;
import java.util.Map;

/**
 * 断言工具类
 *
 * @author Joseph
 * @since 2022/3/13
 */
public class AssertKit {

    public static void check(boolean b, String errorMsg) {
        if (b) {
            throw new BusinessException(errorMsg);
        }
    }

    public static void gt0(Number number, String errorMsg) {
        if (NumberKit.lte0(number)) {
            throw new BusinessException(errorMsg);
        }
    }

    public static void gte0(Number number, String errorMsg) {
        if (NumberKit.lt0(number)) {
            throw new BusinessException(errorMsg);
        }
    }

    public static void lt0(Number number, String errorMsg) {
        if (NumberKit.gte0(number)) {
            throw new BusinessException(errorMsg);
        }
    }

    public static void lte0(Number number, String errorMsg) {
        if (NumberKit.gt0(number)) {
            throw new BusinessException(errorMsg);
        }
    }

    public static void notNull(Object o, String errorMsg) {
        if (null == o) {
            throw new BusinessException(errorMsg);
        }
    }

    public static void notBlank(String s, String errorMsg) {
        if (null == s || s.length() == 0) {
            throw new BusinessException(errorMsg);
        }
    }

    public static <T> void notEmpty(Collection<T> c, String errorMsg) {
        if (null == c || c.size() == 0) {
            throw new BusinessException(errorMsg);
        }
    }

    public static <K, V> void notEmpty(Map<K, V> map, String errorMsg) {
        if (null == map || map.size() == 0) {
            throw new BusinessException(errorMsg);
        }
    }

}
