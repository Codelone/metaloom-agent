package com.metaloom.ai.controller;

import com.metaloom.metadata.agent.MetadataAgent;
import com.metaloom.metadata.model.MetadataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 元数据查询控制器
 */
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    @Autowired
    private MetadataAgent metadataAgent;

    /**
     * 处理元数据查询请求（SSE流式返回）
     * 
     * @param request 元数据请求
     * @return 流式文本
     */
    @PostMapping(value = "/query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> query(@RequestBody MetadataRequest request) {
        return metadataAgent.processQuery(request);
    }
} 
