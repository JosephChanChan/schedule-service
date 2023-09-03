package com.mixc.cpms.schedule.mq.service.controller;

import com.mixc.cpms.schedule.mq.client.dto.DelayedMsgDTO;
import com.mixc.cpms.schedule.mq.client.dto.ResponseCode;
import com.mixc.cpms.schedule.mq.client.dto.Result;
import com.mixc.cpms.schedule.mq.client.dto.SaveMsgRes;
import com.mixc.cpms.schedule.mq.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务外部请求的控制器
 *
 * @author Joseph
 * @since 2023/5/30
 */
@Slf4j
@RestController
@RequestMapping("serve")
@RequiredArgsConstructor
public class ServeClientController {

    private final ScheduleController scheduleController;


    @PostMapping("putMsg")
    public Result<SaveMsgRes> putMsg(@RequestBody DelayedMsgDTO dto) {
        log.info("accept new message dto={}", dto);
        dto.checkParams();

        try {
            SaveMsgRes saveMsgRes = scheduleController.putDelayedMsg(dto);
            log.info("put new message res={}", saveMsgRes);
            return Result.ok(saveMsgRes);
        }
        catch (Exception e) {
            if (e instanceof BusinessException) {
                return Result.error(ResponseCode.BUSINESS_ERROR.getCode(), ((BusinessException) e).getMsg());
            }
            return Result.error();
        }

    }
}
