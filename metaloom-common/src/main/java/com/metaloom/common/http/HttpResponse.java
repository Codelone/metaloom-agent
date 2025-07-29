package com.metaloom.common.http;

import java.util.Map;

/**
 * HTTP 响应结果封装类
 */
public class HttpResponse<T> {

    /**
     * 响应状态码
     */
    private int statusCode;

    /**
     * 响应头
     */
    private Map<String, String> headers;

    /**
     * 响应体
     */
    private T body;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 是否成功
     */
    private boolean success;

    public HttpResponse() {
    }

    public HttpResponse(int statusCode, T body) {
        this.statusCode = statusCode;
        this.body = body;
        this.success = statusCode >= 200 && statusCode < 300;
    }

    public HttpResponse(int statusCode, T body, String message) {
        this.statusCode = statusCode;
        this.body = body;
        this.message = message;
        this.success = statusCode >= 200 && statusCode < 300;
    }

    public HttpResponse(int statusCode, Map<String, String> headers, T body, String message) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.message = message;
        this.success = statusCode >= 200 && statusCode < 300;
    }

    public static <T> HttpResponse<T> success(T body) {
        return new HttpResponse<>(200, body, "Success");
    }

    public static <T> HttpResponse<T> success(int statusCode, T body) {
        return new HttpResponse<>(statusCode, body, "Success");
    }

    public static <T> HttpResponse<T> error(int statusCode, String message) {
        return new HttpResponse<>(statusCode, null, message);
    }

    public static <T> HttpResponse<T> error(int statusCode, String message, T body) {
        return new HttpResponse<>(statusCode, body, message);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        this.success = statusCode >= 200 && statusCode < 300;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", headers=" + headers +
                ", body=" + body +
                ", message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}