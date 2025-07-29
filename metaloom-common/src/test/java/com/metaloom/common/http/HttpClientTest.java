package com.metaloom.common.http;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP 客户端测试类
 */
public class HttpClientTest {

    @Test
    public void testGetRequest() {
        HttpClient client = HttpClient.getDefault();
        HttpResponse<String> response = client.get("https://httpbin.org/get");
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testPostRequest() {
        HttpClient client = HttpClient.getDefault();
        String jsonBody = "{\"test\":\"value\"}";
        HttpResponse<String> response = client.post("https://httpbin.org/post", jsonBody);
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testPostJsonRequest() {
        HttpClient client = HttpClient.getDefault();
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("name", "test");
        jsonData.put("value", 123);
        
        HttpResponse<String> response = client.postJson("https://httpbin.org/post", jsonData);
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetWithParams() {
        HttpClient client = HttpClient.getDefault();
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");
        
        HttpResponse<String> response = client.get("https://httpbin.org/get", params);
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testHeaders() {
        HttpClient client = HttpClient.getDefault();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Test-Header", "test-value");
        
        HttpResponse<String> response = client.get("https://httpbin.org/headers", null, headers);
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testHttpClientUtils() {
        String response = HttpClientUtils.get("https://httpbin.org/get");
        assertNotNull(response);
        assertTrue(response.contains("httpbin.org"));
    }
}