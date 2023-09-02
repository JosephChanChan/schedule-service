package com.mixc.cpms.schedule.mq.client.dto;

import lombok.Data;

@Data
public class Result<T> {

    /**
     * 编码，SUCCESS代表成功
     */
    private String code = "SUCCESS";

    /**
     * 消息内容
     */
    private String msg = "SUCCESS";

    /**
     * 响应数据
     */
    private T data;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error() {
        return error(ResponseCode.UNKNOWN_ERROR.code, ResponseCode.UNKNOWN_ERROR.msg);
    }

    public static <T> Result<T> error(String msg) {
        return error(ResponseCode.UNKNOWN_ERROR.getCode(), msg);
    }

    public static <T> Result<T> error(String code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }


}
