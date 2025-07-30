package com.metaloom.common.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 客户端使用示例
 */
public class HttpClientExample {

    public static void main(String[] args) {
        // 示例1：使用 HttpClientUtils 工具类
        example1();
        
        // 示例2：使用 HttpClient 类
        example2();
    }

    private static void example1() {
        System.out.println("=== 使用 HttpClientUtils 工具类 ===");
        
        try {
            // GET 请求示例
            String getResponse = HttpClientUtils.get("https://httpbin.org/get");
            System.out.println("GET 响应: " + getResponse);
            
            // POST 请求示例
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            String postResponse = HttpClientUtils.post("https://httpbin.org/post", "{\"key\":\"value\"}", headers);
            System.out.println("POST 响应: " + postResponse);
            
        } catch (Exception e) {
            System.err.println("请求失败: " + e.getMessage());
        }
    }

    private static void example2() {
        System.out.println("\n=== 使用 HttpClient 类 ===");
        
        HttpClient client = HttpClient.getDefault()
                .setConnectTimeout(5000)
                .setReadTimeout(10000)
                .addDefaultHeader("Authorization", "Bearer token123");
        
        try {
            // GET 请求示例
            HttpResponse<String> getResponse = client.get("https://httpbin.org/get");
            System.out.println("GET 状态码: " + getResponse.getStatusCode());
            System.out.println("GET 响应: " + getResponse.getBody());
            
            // POST JSON 请求示例
            Map<String, Object> jsonData = new HashMap<>();
            jsonData.put("name", "张三");
            jsonData.put("age", 25);
            HttpResponse<String> postResponse = client.postJson("https://httpbin.org/post", jsonData);
            System.out.println("POST 状态码: " + postResponse.getStatusCode());
            System.out.println("POST 响应: " + postResponse.getBody());
            
            // 带参数的 GET 请求
            Map<String, String> params = new HashMap<>();
            params.put("param1", "value1");
            params.put("param2", "value2");
            HttpResponse<String> getWithParams = client.get("https://httpbin.org/get", params);
            System.out.println("带参数的 GET 状态码: " + getWithParams.getStatusCode());
            
        } catch (Exception e) {
            System.err.println("请求失败: " + e.getMessage());
        }
    }
}