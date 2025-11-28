package com.metaloom.model.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        // 创建并配置 Netty 的 HttpClient
        HttpClient httpClient = HttpClient.create()
                .headers(headers -> headers
                        .remove("Transfer-Encoding")  // 显式移除分块头
                        .set("Connection", "close")   // 强制关闭连接避免分块
                )
                .compress(false);  // 禁用压缩

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}