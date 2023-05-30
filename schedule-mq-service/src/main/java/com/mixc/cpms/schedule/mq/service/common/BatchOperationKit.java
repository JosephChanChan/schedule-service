package com.mixc.cpms.schedule.mq.service.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Joseph
 * @since 2023/1/23
 */
public class BatchOperationKit {

    /**
     * 分页执行函数
     *
     * @param total 总数目
     * @param bulkSize 批量执行数量
     * @param function 执行函数，第一个参数是起始位，第二个是每次批量执行数量
     * @param <R> 结果
     * @return list
     */
    public static <R> List<R> pagingExecute(int total, int bulkSize, Function<Integer, R> function) {
        if (bulkSize >= total) {
            List<R> list = new ArrayList<>(1);
            list.add(function.apply(0));
            return list;
        }
        int pageCount = total / bulkSize + (total % bulkSize == 0 ? 0 : 1);
        List<R> ans = new ArrayList<>(pageCount);
        for (int i = 0; i < pageCount; i++) {
            R result = function.apply(i * bulkSize);
            if (null != result) {
                ans.add(result);
            }
        }
        return ans;
    }

    /**
     * 分批执行函数
     *
     * @param total 总数目
     * @param bulkSize 批量执行数量
     * @param function 执行函数，第一个参数是起始位，第二个是每次批量执行数量
     */
    public static void batchExecute(int total, int bulkSize, Consumer<Integer> function) {
        if (bulkSize >= total) {
            function.accept(0);
        }
        int pageCount = total / bulkSize + (total % bulkSize == 0 ? 0 : 1);
        for (int i = 0; i < pageCount; i++) {
            function.accept(i * bulkSize);
        }
    }
}
