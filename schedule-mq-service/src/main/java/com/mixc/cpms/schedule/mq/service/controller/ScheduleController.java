package com.mixc.cpms.schedule.mq.service.controller;

import com.mixc.cpms.schedule.mq.client.kit.AssertKit;
import com.mixc.cpms.schedule.mq.client.kit.NumberKit;
import com.mixc.cpms.schedule.mq.service.cache.MsgDeliverInfoHolder;
import com.mixc.cpms.schedule.mq.service.cache.ScheduleOffsetHolder;
import com.mixc.cpms.schedule.mq.service.cache.TimeBucketWheel;
import com.mixc.cpms.schedule.mq.service.common.*;
import com.mixc.cpms.schedule.mq.service.config.ApplicationConfig;
import com.mixc.cpms.schedule.mq.service.enums.ServerStatus;
import com.mixc.cpms.schedule.mq.service.job.*;
import com.mixc.cpms.schedule.mq.service.model.DelayedMsg;
import com.mixc.cpms.schedule.mq.service.model.MsgItem;
import com.mixc.cpms.schedule.mq.service.model.ScheduleOffset;
import com.mixc.cpms.schedule.mq.client.dto.DelayedMsgDTO;
import com.mixc.cpms.schedule.mq.client.dto.SaveMsgRes;
import com.mixc.cpms.schedule.mq.service.model.dto.TimeSegmentDTO;
import com.mixc.cpms.schedule.mq.service.service.IDistributionLockService;
import com.mixc.cpms.schedule.mq.service.service.IMQDispatcher;
import com.mixc.cpms.schedule.mq.service.service.IScheduleOffsetService;
import com.mixc.cpms.schedule.mq.service.service.ITimeBucketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 时间轮控制器
 *
 * @author Joseph
 * @since 2023/1/20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleController {

    /**
     * 服务状态。
     * INITIALING -> RUNNING -> CLOSING -> TERMINATED
     */
    private AtomicReference<String> status ;

    /**
     * 当前时间轮
     */
    private volatile TimeBucketWheel wheelRolling ;

    /**
     * 下一个时间轮
     */
    private volatile TimeBucketWheel wheelNext ;

    /**
     * MQ消息分派器
     */
    private final IMQDispatcher mqDispatcher;

    /**
     * 系统配置类
     */
    private final ApplicationConfig config;

    /**
     * 时间片逻辑服务，负责时间片的读写消息
     */
    private final ITimeBucketService timeBucketService;

    /**
     * 调度偏移量服务
     */
    private final IScheduleOffsetService scheduleOffsetService;

    /**
     * 分布式锁服务
     */
    private final IDistributionLockService distributionLockService;

    /**
     * 消息投递情况缓存类
     */
    private final MsgDeliverInfoHolder msgDeliverInfoHolder;

    /**
     * 调度偏移量缓存类
     */
    private final ScheduleOffsetHolder scheduleOffsetHolder;

    /* ---------------------------------------------- Thread Service -------------------------------------------- */

    private RollingTimeService rollingTimeService ;

    private RetryDeliverService retryDeliverService ;

    private PreloadTimeSegmentService preloadTimeSegmentService ;

    private FlushScheduleOffsetService flushScheduleOffsetService ;

    private CheckTimeSegmentService checkTimeSegmentService ;



    /**
     * 服务启动后，开始初始化动作
     */
    public void initialize() {
        this.status = new AtomicReference<>(ServerStatus.INITIALING.name());
        // 开始恢复服务
        recover();
        // 初始化缓存器
        initializeHolder();
        // 开启各类线程
        startThread();
        // 服务此时可接受消息写入
        changeStatus(ServerStatus.RUNNING);
    }

    private void recover() {
        initializeTimeSegment();

        ScheduleOffset offset = scheduleOffsetService.getOffset(config.getScheduleServiceCode());
        if (null == offset) {
            initialRecover();
            return;
        }

        String segmentName = LocalDateTime.now().format(DateTimeFormat.yyyyMMddHHmm);
        long nowTime = Long.parseLong(segmentName);
        long segmentOffset = Long.parseLong(offset.getScheduleOffset().split("-")[0]);
        long idOffset = Long.parseLong(offset.getScheduleOffset().split("-")[1]);

        if (nowTime <= segmentOffset) {
            normalRecover(segmentOffset, idOffset);
        }
        else {
            abnormalRecover(segmentOffset, idOffset);
        }
    }

    /**
     * 初始化恢复。没有offset，读当前时间片
     */
    private void initialRecover() {
        Long time = Long.parseLong(LocalDateTime.now().format(DateTimeFormat.yyyyMMddHHmm));
        log.info("ScheduleController initialRecover");

        List<Long> segments = timeBucketService.showAllSegments();
        // 能cover当前时间的时间片
        int minIdx = CollectionsKit.binarySearchFloor(time, segments, true);
        Long segment = segments.get(minIdx);

        normalRecover(segment, 0);
        log.info("ScheduleController initialRecover done");
    }

    /**
     * 正常恢复，当前时间和最后保存的offset匹配上。直接读取时间片的msg到时间轮
     *
     * @param segmentOffset 时间片offset
     * @param idOffset Msg Id offset
     */
    private void normalRecover(long segmentOffset, long idOffset) {
        log.info("ScheduleController normalRecover segmentOffset={} idOffset={}", segmentOffset, idOffset);

        List<Long> segments = timeBucketService.showAllSegments();
        AssertKit.notEmpty(segments, "时间片集合不能为空");
        log.info("ScheduleController normalRecover segments={}", segments);

        TimeSegmentDTO timeSegmentDTO = timeBucketService.loadSegmentFrom(String.valueOf(segmentOffset), idOffset);
        Long wheelTimeRight = timeSegmentDTO.getTimeBoundRight();

        // 二分搜索时间片中小于当前时间轮的最大时间片
        int maxIdx = CollectionsKit.binarySearchCeiling(wheelTimeRight, segments);
        // 如果只有一个时间片，当前时间轮的leftTime和rightTime就一致
        Long wheelTimeLeft = NumberKit.gte0(maxIdx) ? segments.get(maxIdx) : segmentOffset;

        this.wheelRolling = new TimeBucketWheel(wheelTimeLeft, wheelTimeRight, mqDispatcher);
        // 初始化时间轮
        if (CollectionsKit.isNotEmpty(timeSegmentDTO.getDelayedMsgList())) {
            List<MsgItem> msgIndexList = timeSegmentDTO.getDelayedMsgList()
                    .stream()
                    .map(DelayedMsg::convert)
                    .collect(Collectors.toList());
            this.wheelRolling.initialize(msgIndexList);
        }
        log.info("ScheduleController normalRecover wheelRolling initialized {}", wheelRolling.getTimeBucket());

        // wheelNext时间轮让Preload线程预加载
        log.info("ScheduleController normalRecover done");
    }

    /**
     * 异常恢复，保存的offset和真实时间，差距 > 1个时间片。分页投递msg，直到追上当前时间的时间片
     *
     * @param segmentOffset 时间片offset
     * @param idOffset id offset
     */
    private void abnormalRecover(long segmentOffset, long idOffset) {
        log.info("ScheduleController abnormalRecover segmentOffset={} idOffset={}", segmentOffset, idOffset);

        // 首先要知道有哪些时间片
        List<Long> segments = timeBucketService.showAllSegments();
        AssertKit.notEmpty(segments, "时间片集合不能为空");
        log.info("ScheduleController abnormalRecover segments={}", segments);

        // 保留下要追赶的时间片
        segments = segments.stream().filter(segment -> segment >= segmentOffset).collect(Collectors.toList());
        segments.sort((o1 ,o2) -> (int) (o1 - o2));

        // 分页读取并交给MQDispatcher投递
        for (int i = 0; i < segments.size(); i++) {
            Long time = segments.get(i);
            String segment = String.valueOf(time);
            log.info("ScheduleController abnormalRecover load segment={}", time);

            if (i == segments.size()-1) {
                normalRecover(time, 0);
                return;
            }

            /*
                确定好每个时间片的最大ID，分批读出1000条MsgId交给MQDispatcher异步投递
                很快主线程回到这里再次读出1000条，再次提交
             */

            long startId = 0;
            Long maxId = timeBucketService.maxIdFromSegment(segment);
            while (startId < maxId) {
                long toId = startId + Constant.BATCH_READ_DELAYED_MSG_SIZE;
                TimeSegmentDTO page = timeBucketService.loadSegmentInRange(segment, startId, toId);

                List<DelayedMsg> delayedMsgList = page.getDelayedMsgList();
                mqDispatcher.submit(segment, delayedMsgList.stream().map(DelayedMsg::getId).collect(Collectors.toList()));

                startId = toId;
            }
            log.info("ScheduleController abnormalRecover process done segment={}", segment);
        }
    }

    /**
     * 初始化时间片表，一次性创建连续的从当前时间起覆盖30天的时间片
     */
    private void initializeTimeSegment() {
        timeBucketService.initializeSegments();
    }

    private void changeStatus(ServerStatus serverStatus) {
        String val = this.status.get();
        log.info("ScheduleController changeStatus nowStatus={} toStatus={}", val, serverStatus);
        while (!this.status.compareAndSet(val, serverStatus.name())) {
            val = this.status.get();
        }
        log.info("ScheduleController changeStatus done oldStatus={} toStatus={}", val, serverStatus);
    }

    public boolean serverReady() {
        return null == this.status || Objects.equals(this.status.get(), ServerStatus.RUNNING.name());
    }

    /**
     * 延迟消息写到对应时间轮
     *
     * @return 消息偏移量，时间片名称-偏移量id
     */
    public SaveMsgRes putDelayedMsg(DelayedMsgDTO dto) {
        if (!serverReady()) {
            log.warn("TimeBucket putDelayedMsg server not ready yet");
            return null;
        }

        SaveMsgRes saveMsgRes = timeBucketService.putDelayedMsg(config.getScheduleServiceCode(), dto);

        MsgItem msgItem = MsgItem.build(saveMsgRes.id, dto);
        Long deadlineSeconds = msgItem.getDeadlineSeconds();

        if (deadlineSeconds <= wheelRolling.getTimeBoundRightSec()) {
            // 即使wheel此时发生了切换，会抛出异常让客户端重试
            wheelRolling.put(msgItem);
        }
        else if (null != wheelNext && deadlineSeconds <= wheelNext.getTimeBoundRightSec()) {
            // 即使wheel此时发生了切换，会抛出异常让客户端重试
            wheelNext.put(msgItem);
        }

        return saveMsgRes;
    }

    private void initializeHolder() {
        this.scheduleOffsetHolder.initialize(wheelRolling.getTimeBoundRight());
        this.msgDeliverInfoHolder.initialize();
    }

    private void startThread() {
        this.rollingTimeService = new RollingTimeService();
        this.retryDeliverService = new RetryDeliverService();
        this.preloadTimeSegmentService = new PreloadTimeSegmentService();
        this.flushScheduleOffsetService = new FlushScheduleOffsetService();
        this.checkTimeSegmentService = new CheckTimeSegmentService();

        rollingTimeService.start(wheelRolling, this, preloadTimeSegmentService);
        retryDeliverService.start(msgDeliverInfoHolder, mqDispatcher);
        preloadTimeSegmentService.start(timeBucketService, mqDispatcher, this);
        flushScheduleOffsetService.start(scheduleOffsetHolder);
        checkTimeSegmentService.start(timeBucketService, distributionLockService);
    }

    /**
     * 资源清理工作。数据保存工作
     */
    public void stopServer() {
        log.info("ScheduleController stopServer begin");

        // 停止新消息写入
        changeStatus(ServerStatus.CLOSING);

        preloadTimeSegmentService.stop();
        checkTimeSegmentService.stop();
        rollingTimeService.stop();
        retryDeliverService.stop();
        flushScheduleOffsetService.stop();

        // 未来上面的收尾工作改为异步，这里要做同步等待一段时间

        // 最后状态推到终结
        changeStatus(ServerStatus.TERMINATED);

        log.info("ScheduleController stopServer done");
    }

    public void swapWheel(Consumer<TimeBucketWheel> consumer) {
        // RollingTimeService的wheel指向wheelNext
        consumer.accept(wheelNext);

        TimeBucketWheel waitClear = wheelRolling;
        wheelRolling = wheelNext;

        // wheelNext置空，Preload线程预加载下一个时间片，wheelNext指向下下个wheel
        this.wheelNext = null;

        // 清理上一个wheel，此时上一个wheel应无引用，会被GC
        if (null != waitClear) {
            waitClear.clear();
        }

        scheduleOffsetHolder.nextTime(wheelRolling.getTimeBoundRight());
    }

    public void setWheelNext(TimeBucketWheel wheel) {
        this.wheelNext = wheel;
    }

    public void setWheelRolling(TimeBucketWheel wheel) {
        this.wheelRolling = wheel;
    }

    public boolean hasWheelNext() {
        return null != this.wheelNext;
    }

    public boolean hasWheelRolling() {
        return null != this.wheelRolling;
    }

    /**
     * 该方法只会有PreloadService调用
     */
    public Long getWheelRollingTime() {
        if (null == this.wheelRolling) {
            // wheelRolling是空，返回当前时间，让PreloadService加载>当前时间的时间片
            return Long.parseLong(LocalDateTime.now().format(DateTimeFormat.yyyyMMddHHmm));
        }
        return this.wheelRolling.getTimeBoundRight();
    }


}
