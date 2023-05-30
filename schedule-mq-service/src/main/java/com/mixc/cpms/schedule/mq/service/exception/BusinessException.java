package com.mixc.cpms.schedule.mq.service.exception;

import com.mixc.cpms.schedule.mq.service.enums.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = 1L;

    private int code;
	private String msg;

	public BusinessException(String msg) {
		super(msg);
		this.code = ErrorCode.BUSINESS_ERROR.getCode();
		this.msg = msg;
	}

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMsg());
		this.code = errorCode.getCode();
		this.msg = errorCode.getMsg();
	}

	public BusinessException(String msg, Throwable e) {
		super(msg, e);
		this.code = ErrorCode.BUSINESS_ERROR.getCode();
		this.msg = msg;
	}

	public BusinessException(int code , String msg) {
		super(msg);
		this.code = code;
		this.msg = msg;
	}

}