package com.metaloom.ai.controller;

import com.metaloom.metadata.agent.MetadataAgent;
import com.metaloom.metadata.model.MetadataRequest;
import com.metaloom.metadata.model.MetadataResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 元数据查询控制器
 */
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    @Autowired
    private MetadataAgent metadataAgent;

    /**
     * 处理元数据查询请求
     * 
     * @param request 元数据请求
     * @return 元数据响应
     */
    @PostMapping("/query")
    public MetadataResponse query(@RequestBody MetadataRequest request) {
        return metadataAgent.processQuery(request);
    }
} 
