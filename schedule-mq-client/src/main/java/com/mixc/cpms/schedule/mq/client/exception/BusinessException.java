
package com.mixc.cpms.schedule.mq.client.exception;

import lombok.Data;

/**
 * 自定义异常
 *
 * @author Joseph
 */
@Data
public class BusinessException extends RuntimeException {

    private String msg;
    private int code = -1;

    public BusinessException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public BusinessException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public BusinessException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public BusinessException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }


}
