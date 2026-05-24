package com.iapp.iapp_messenger.dao.dto;

public class WsResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public WsResponse() {}

    public WsResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }

    public String getMessage() { return message; }

    public T getData() { return data; }

    public static <T> WsResponse<T> ok(T data) {
        return new WsResponse<>(true, null, data);
    }

    public static <T> WsResponse<T> error(String msg) {
        return new WsResponse<>(false, msg, null);
    }
}
