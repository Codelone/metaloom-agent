package com.metaloom.ai.controller;

import com.metaloom.lineage.agent.LineageAgent;
import com.metaloom.lineage.model.LineageRequest;
import com.metaloom.lineage.model.LineageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 血缘关系查询控制器
 */
@RestController
@RequestMapping("/api/lineage")
public class LineageController {

    @Autowired
    private LineageAgent lineageAgent;

    /**
     * 处理血缘关系查询请求
     * 
     * @param request 血缘查询请求
     * @return 血缘查询响应
     */
    @PostMapping("/query")
    public LineageResponse query(@RequestBody LineageRequest request) {
        return lineageAgent.processQuery(request);
    }
} 
