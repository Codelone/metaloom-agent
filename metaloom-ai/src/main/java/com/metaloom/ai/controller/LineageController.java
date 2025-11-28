package com.metaloom.ai.controller;

import com.metaloom.lineage.agent.LineageAgent;
import com.metaloom.lineage.model.LineageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 血缘关系查询控制器
 */
@RestController
@RequestMapping("/api/lineage")
public class LineageController {

    @Autowired
    private LineageAgent lineageAgent;

    /**
     * 处理血缘关系查询请求（SSE流式返回）
     * 
     * @param request 血缘查询请求
     * @return 流式文本
     */
    @PostMapping(value = "/query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> query(@RequestBody LineageRequest request) {
        return lineageAgent.processQuery(request);
    }
} 
