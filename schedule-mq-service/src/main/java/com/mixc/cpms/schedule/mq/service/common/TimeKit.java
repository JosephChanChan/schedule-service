package com.mixc.cpms.schedule.mq.service.common;

import com.mixc.cpms.schedule.mq.client.kit.AssertKit;
import com.mixc.cpms.schedule.mq.service.exception.BusinessException;
import lombok.Data;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;


/**
 * 时间工具类
 *
 * @author Joseph
 * @since 2022/12/8
 */
public class TimeKit {

    public static final int ONE_SECOND = 1000;

    private static final long ONE_YEAR = 1000L * 60 * 60 * 24 * 365;
    private static final long ONE_MONTH = 1000L * 60 * 60 * 24 * 30;
    private static final long ONE_DAY = 1000 * 60 * 60 * 24;
    private static final long ONE_HOUR = 1000 * 60 * 60;
    private static final long ONE_MINUTE = 1000 * 60;



    public static long convertSeconds(Long time) {
        return LocalDateTime.parse(String.valueOf(time), DateTimeFormat.yyyyMMddHHmm).toEpochSecond(ZoneOffset.ofHours(8));
    }

    public static long nowSeconds() {
        return System.currentTimeMillis() / ONE_SECOND;
    }

    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    public static String plus30Minutes(Long time) {
        return LocalDateTime.parse(String.valueOf(time),
                DateTimeFormat.yyyyMMddHHmm).plusMinutes(30).format(DateTimeFormat.yyyyMMddHHmm);
    }

    public static String plusTime(Long time, Long diff, TimeUnit timeUnit, DateTimeFormatter formatter) {
        LocalDateTime dateTime = LocalDateTime.parse(String.valueOf(time), formatter);
        switch (timeUnit) {
            case SECOND:
                dateTime = dateTime.plusSeconds(diff);
                break;
            case MINUTE:
                dateTime = dateTime.plusMinutes(diff);
                break;
            case HOUR:
                dateTime = dateTime.plusHours(diff);
                break;
            case DAY:
                dateTime = dateTime.plusDays(diff);
                break;
            case MONTH:
                dateTime = dateTime.plusMonths(diff);
                break;
            case YEAR:
                dateTime = dateTime.plusYears(diff);
        }
        return dateTime.format(formatter);
    }

    /**
     * 计算起始时间和结束时间的差值，按照 Y年M月D天 h时m分s秒的差值返回
     */
    public static TimeCarrier timeDiff(LocalDateTime start, LocalDateTime end) {
        return timeDiff(start, end, null);
    }

    public static TimeCarrier timeDiff(Date start, Date end) {
        AssertKit.notNull(start, "开始时间不能为空");
        AssertKit.notNull(end, "结束时间不能为空");
        return timeDiff(
                LocalDateTime.ofInstant(start.toInstant(), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(end.toInstant(), ZoneId.systemDefault()),
                null);
    }

    /**
     * 计算起始时间和结束时间的差值，按照 Y年M月D天 h时m分s秒的差值返回
     * 提供自定义选择返回需要的字段。例如只返回 Y年0月D天 0时m分0秒，即只有需要的字段会返回计算好的差值，其余为-1
     * timeStructure提供字段选择功能
     */
    public static TimeCarrier timeDiff(LocalDateTime start, LocalDateTime end, TimeStructure timeStructure) {
        AssertKit.notNull(start, "开始时间不能为空");
        AssertKit.notNull(end, "结束时间不能为空");
        AssertKit.check(start.isAfter(end), "结束时间不能小于开始时间");

        if (start.isEqual(end)) {
            return new TimeCarrier();
        }

        int[] ans = new int[TimeUnit.values().length];
        Arrays.fill(ans, -1);

        doCalc(start, end, ans);
        return TimeCarrier.build(ans, timeStructure);
    }

    private static void doCalc(LocalDateTime start, LocalDateTime end, int[] ans) {
        Duration duration = Duration.between(start, end);
        long millis = duration.toMillis();

        for (TimeUnit timeUnit : TimeUnit.values()) {
            switch (timeUnit) {
                case YEAR:
                    ans[timeUnit.bit] = (int) (millis / ONE_YEAR);
                    millis %= ONE_YEAR;
                    break;

                case MONTH:
                    ans[timeUnit.bit] = (int) (millis / ONE_MONTH);
                    millis %= ONE_MONTH;
                    break;

                case DAY:
                    ans[timeUnit.bit] = (int) (millis / ONE_DAY);
                    millis %= ONE_DAY;
                    break;

                case HOUR:
                    ans[timeUnit.bit] = (int) (millis / ONE_HOUR);
                    millis %= ONE_HOUR;
                    break;

                case MINUTE:
                    ans[timeUnit.bit] = (int) (millis / ONE_MINUTE);
                    millis %= ONE_MINUTE;
                    break;

                case SECOND:
                    ans[timeUnit.bit] = (int) (millis / 1000);
                    break;

                default:
                    throw new BusinessException(String.format("TimeUnit=%s 没有找到对应枚举", timeUnit.getBit()));
            }
        }
    }


    @Data
    public static class TimeCarrier {
        private int year;
        private int month;
        private int day;
        private int hour;
        private int minute;
        private int second;

        public static TimeCarrier build(int[] ans, TimeStructure timeStructure) {
            TimeCarrier carrier = new TimeCarrier();
            for (int i = 0; i < ans.length; i++) {
                if (ans[i] > 0) {
                    switch (TimeUnit.of(i)) {
                        case YEAR:
                            if (null == timeStructure || timeStructure.hitUnit(TimeUnit.of(i))) {
                                carrier.setYear(ans[i]);
                            }
                            break;
                        case MONTH:
                            if (null == timeStructure || timeStructure.hitUnit(TimeUnit.of(i))) {
                                carrier.setMonth(ans[i]);
                            }
                            break;
                        case DAY:
                            if (null == timeStructure || timeStructure.hitUnit(TimeUnit.of(i))) {
                                carrier.setDay(ans[i]);
                            }
                            break;
                        case HOUR:
                            if (null == timeStructure || timeStructure.hitUnit(TimeUnit.of(i))) {
                                carrier.setHour(ans[i]);
                            }
                            break;
                        case MINUTE:
                            if (null == timeStructure || timeStructure.hitUnit(TimeUnit.of(i))) {
                                carrier.setMinute(ans[i]);
                            }
                            break;
                        case SECOND:
                            if (null == timeStructure || timeStructure.hitUnit(TimeUnit.of(i))) {
                                carrier.setSecond(ans[i]);
                            }
                    }
                }
            }
            return carrier;
        }
    }

    public static class TimeStructure {
        private byte timeBit = 0;

        public static TimeStructure build() {
            return new TimeStructure();
        }

        public TimeStructure setUnit(TimeUnit timeUnit) {
            this.timeBit = (byte) (this.timeBit | (1 << timeUnit.getBit()));
            return this;
        }

        public boolean hitUnit(TimeUnit timeUnit) {
            return (timeBit & (1 << timeUnit.getBit())) > 0;
        }
    }

    @Getter
    public enum TimeUnit {
        YEAR(0),
        MONTH(1),
        DAY(2),
        HOUR(3),
        MINUTE(4),
        SECOND(5)
        ;
        private int bit;
        TimeUnit(int bit) {
            this.bit = bit;
        }
        public static TimeUnit of(int bit) {
            for (TimeUnit timeUnit : values()) {
                if (bit == timeUnit.getBit()) {
                    return timeUnit;
                }
            }
            throw new BusinessException(String.format("TimeUnit=%s 没有找到对应枚举", bit));
        }
    }
}
