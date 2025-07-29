package com.metaloom.common.http;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 高级 HTTP 客户端类
 * 提供更简洁的 API 和更多的功能
 */
public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    private int connectTimeout = 5000;
    private int readTimeout = 10000;
    private int retryTimes = 3;
    private Map<String, String> defaultHeaders = new HashMap<>();

    public HttpClient() {
        // 默认请求头
        defaultHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
    }

    /**
     * 设置连接超时时间
     * @param connectTimeout 连接超时时间（毫秒）
     * @return this
     */
    public HttpClient setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 设置读取超时时间
     * @param readTimeout 读取超时时间（毫秒）
     * @return this
     */
    public HttpClient setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * 设置重试次数
     * @param retryTimes 重试次数
     * @return this
     */
    public HttpClient setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    /**
     * 添加默认请求头
     * @param key 请求头名称
     * @param value 请求头值
     * @return this
     */
    public HttpClient addDefaultHeader(String key, String value) {
        this.defaultHeaders.put(key, value);
        return this;
    }

    /**
     * 移除默认请求头
     * @param key 请求头名称
     * @return this
     */
    public HttpClient removeDefaultHeader(String key) {
        this.defaultHeaders.remove(key);
        return this;
    }

    /**
     * GET 请求
     * @param url 请求URL
     * @return 响应结果
     */
    public HttpResponse<String> get(String url) {
        return get(url, null, null);
    }

    /**
     * GET 请求
     * @param url 请求URL
     * @param params 请求参数
     * @return 响应结果
     */
    public HttpResponse<String> get(String url, Map<String, String> params) {
        return get(url, params, null);
    }

    /**
     * GET 请求
     * @param url 请求URL
     * @param params 请求参数
     * @param headers 请求头
     * @return 响应结果
     */
    public HttpResponse<String> get(String url, Map<String, String> params, Map<String, String> headers) {
        try {
            String response = HttpClientUtils.get(url, params, mergeHeaders(headers));
            return HttpResponse.success(200, response);
        } catch (Exception e) {
            logger.error("GET request failed: {}", e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }

    /**
     * POST 请求
     * @param url 请求URL
     * @param body 请求体
     * @return 响应结果
     */
    public HttpResponse<String> post(String url, String body) {
        return post(url, body, null);
    }

    /**
     * POST 请求
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @return 响应结果
     */
    public HttpResponse<String> post(String url, String body, Map<String, String> headers) {
        try {
            String response = HttpClientUtils.post(url, body, mergeHeaders(headers));
            return HttpResponse.success(200, response);
        } catch (Exception e) {
            logger.error("POST request failed: {}", e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }

    /**
     * POST JSON 请求
     * @param url 请求URL
     * @param jsonData JSON数据
     * @return 响应结果
     */
    public HttpResponse<String> postJson(String url, Object jsonData) {
        return postJson(url, jsonData, null);
    }

    /**
     * POST JSON 请求
     * @param url 请求URL
     * @param jsonData JSON数据
     * @param headers 请求头
     * @return 响应结果
     */
    public HttpResponse<String> postJson(String url, Object jsonData, Map<String, String> headers) {
        try {
            String response = HttpClientUtils.postJson(url, jsonData, mergeHeaders(headers));
            return HttpResponse.success(200, response);
        } catch (Exception e) {
            logger.error("POST JSON request failed: {}", e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }

    /**
     * PUT 请求
     * @param url 请求URL
     * @param body 请求体
     * @return 响应结果
     */
    public HttpResponse<String> put(String url, String body) {
        return put(url, body, null);
    }

    /**
     * PUT 请求
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @return 响应结果
     */
    public HttpResponse<String> put(String url, String body, Map<String, String> headers) {
        try {
            String response = HttpClientUtils.put(url, body, mergeHeaders(headers));
            return HttpResponse.success(200, response);
        } catch (Exception e) {
            logger.error("PUT request failed: {}", e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }

    /**
     * DELETE 请求
     * @param url 请求URL
     * @return 响应结果
     */
    public HttpResponse<String> delete(String url) {
        return delete(url, null);
    }

    /**
     * DELETE 请求
     * @param url 请求URL
     * @param headers 请求头
     * @return 响应结果
     */
    public HttpResponse<String> delete(String url, Map<String, String> headers) {
        try {
            String response = HttpClientUtils.delete(url, mergeHeaders(headers));
            return HttpResponse.success(200, response);
        } catch (Exception e) {
            logger.error("DELETE request failed: {}", e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }

    /**
     * 合并请求头
     * @param customHeaders 自定义请求头
     * @return 合并后的请求头
     */
    private Map<String, String> mergeHeaders(Map<String, String> customHeaders) {
        Map<String, String> mergedHeaders = new HashMap<>(defaultHeaders);
        if (customHeaders != null && !customHeaders.isEmpty()) {
            mergedHeaders.putAll(customHeaders);
        }
        return mergedHeaders;
    }

    /**
     * 获取默认的 HTTP 客户端实例
     * @return HttpClient 实例
     */
    public static HttpClient getDefault() {
        return new HttpClient();
    }
}