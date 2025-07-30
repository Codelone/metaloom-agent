package com.metaloom.common.http;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 请求工具类
 * 支持 GET、POST、PUT、DELETE 等常用 HTTP 方法
 */
public class HttpClientUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
    
    // 默认连接超时时间（毫秒）
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    // 默认读取超时时间（毫秒）
    private static final int DEFAULT_READ_TIMEOUT = 10000;
    // 默认重试次数
    private static final int DEFAULT_RETRY_TIMES = 3;
    
    /**
     * GET 请求
     * @param url 请求URL
     * @return 响应内容
     */
    public static String get(String url) {
        return get(url, null, null);
    }

    /**
     * GET 请求
     * @param url 请求URL
     * @param params 请求参数
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> params) {
        return get(url, params, null);
    }

    /**
     * GET 请求
     * @param url 请求URL
     * @param params 请求参数
     * @param headers 请求头
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> params, Map<String, String> headers) {
        String fullUrl = buildUrlWithParams(url, params);
        return executeRequest("GET", fullUrl, null, headers, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_RETRY_TIMES);
    }

    /**
     * POST 请求
     * @param url 请求URL
     * @param body 请求体
     * @return 响应内容
     */
    public static String post(String url, String body) {
        return post(url, body, null);
    }

    /**
     * POST 请求
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @return 响应内容
     */
    public static String post(String url, String body, Map<String, String> headers) {
        return executeRequest("POST", url, body, headers, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_RETRY_TIMES);
    }

    /**
     * POST JSON 请求
     * @param url 请求URL
     * @param jsonData JSON数据
     * @return 响应内容
     */
    public static String postJson(String url, Object jsonData) {
        return postJson(url, jsonData, null);
    }

    /**
     * POST JSON 请求
     * @param url 请求URL
     * @param jsonData JSON数据
     * @param headers 请求头
     * @return 响应内容
     */
    public static String postJson(String url, Object jsonData, Map<String, String> headers) {
        if (headers == null) {
            headers = Map.of("Content-Type", "application/json");
        } else {
            headers.put("Content-Type", "application/json");
        }
        return post(url, JSON.toJSONString(jsonData), headers);
    }

    /**
     * PUT 请求
     * @param url 请求URL
     * @param body 请求体
     * @return 响应内容
     */
    public static String put(String url, String body) {
        return put(url, body, null);
    }

    /**
     * PUT 请求
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @return 响应内容
     */
    public static String put(String url, String body, Map<String, String> headers) {
        return executeRequest("PUT", url, body, headers, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_RETRY_TIMES);
    }

    /**
     * DELETE 请求
     * @param url 请求URL
     * @return 响应内容
     */
    public static String delete(String url) {
        return delete(url, null);
    }

    /**
     * DELETE 请求
     * @param url 请求URL
     * @param headers 请求头
     * @return 响应内容
     */
    public static String delete(String url, Map<String, String> headers) {
        return executeRequest("DELETE", url, null, headers, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_RETRY_TIMES);
    }

    /**
     * 执行 HTTP 请求
     * @param method 请求方法
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param connectTimeout 连接超时时间（毫秒）
     * @param readTimeout 读取超时时间（毫秒）
     * @param retryTimes 重试次数
     * @return 响应内容
     */
    private static String executeRequest(String method, String url, String body, Map<String, String> headers, 
                                         int connectTimeout, int readTimeout, int retryTimes) {
        for (int i = 0; i < retryTimes; i++) {
            try {
                return doExecuteRequest(method, url, body, headers, connectTimeout, readTimeout);
            } catch (IOException e) {
                logger.warn("HTTP request failed, retry {}/{}: {}", i + 1, retryTimes, e.getMessage());
                if (i == retryTimes - 1) {
                    throw new RuntimeException("HTTP request failed after " + retryTimes + " attempts", e);
                }
                // 重试间隔
                try {
                    TimeUnit.MILLISECONDS.sleep(100 * (i + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
        throw new RuntimeException("HTTP request failed after " + retryTimes + " attempts");
    }

    /**
     * 执行实际的 HTTP 请求
     * @param method 请求方法
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param connectTimeout 连接超时时间（毫秒）
     * @param readTimeout 读取超时时间（毫秒）
     * @return 响应内容
     * @throws IOException IO异常
     */
    private static String doExecuteRequest(String method, String url, String body, Map<String, String> headers, 
                                         int connectTimeout, int readTimeout) throws IOException {
        java.net.HttpURLConnection connection = null;
        try {
            java.net.URL urlObj = new java.net.URL(url);
            connection = (java.net.HttpURLConnection) urlObj.openConnection();
            
            // 设置请求方法
            connection.setRequestMethod(method);
            
            // 设置超时时间
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            
            // 设置请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            
            // 设置是否允许输出
            if (body != null && ("POST".equals(method) || "PUT".equals(method))) {
                connection.setDoOutput(true);
                connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            }
            
            // 获取响应码
            int responseCode = connection.getResponseCode();
            
            // 读取响应内容
            java.io.InputStream inputStream;
            if (responseCode >= 200 && responseCode < 300) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }
            
            java.io.ByteArrayOutputStream result = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            
            String response = result.toString(StandardCharsets.UTF_8.name());
            
            if (responseCode >= 200 && responseCode < 300) {
                logger.debug("HTTP {} request to {} succeeded with response code {}", method, url, responseCode);
                return response;
            } else {
                logger.warn("HTTP {} request to {} failed with response code {}: {}", method, url, responseCode, response);
                throw new RuntimeException("HTTP request failed with response code " + responseCode + ": " + response);
            }
            
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 构建带参数的 URL
     * @param baseUrl 基础URL
     * @param params 参数Map
     * @return 完整的URL
     */
    private static String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }
        
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        if (!baseUrl.contains("?")) {
            urlBuilder.append("?");
        } else if (!baseUrl.endsWith("?") && !baseUrl.endsWith("&")) {
            urlBuilder.append("&");
        }
        
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                urlBuilder.append("&");
            }
            try {
                urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()))
                         .append("=")
                         .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Failed to encode URL parameters", e);
            }
            first = false;
        }
        
        return urlBuilder.toString();
    }

    /**
     * 设置自定义超时时间
     * @param connectTimeout 连接超时时间（毫秒）
     * @param readTimeout 读取超时时间（毫秒）
     */
    public static void setTimeouts(int connectTimeout, int readTimeout) {
        // 在实际应用中，可以通过配置类来设置这些值
        // 这里提供静态方法是为了方便测试
    }

    /**
     * 设置重试次数
     * @param retryTimes 重试次数
     */
    public static void setRetryTimes(int retryTimes) {
        // 在实际应用中，可以通过配置类来设置这些值
        // 这里提供静态方法是为了方便测试
    }
}