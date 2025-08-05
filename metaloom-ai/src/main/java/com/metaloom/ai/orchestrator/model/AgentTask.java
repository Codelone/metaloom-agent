package com.metaloom.ai.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体任务
 * 用于支持并行执行多个智能体任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTask {
    
    /**
     * 智能体名称
     */
    private String agentName;
    
    /**
     * 请求体
     */
    private String requestBody;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 执行结果
     */
    private String result;
    
    /**
     * 执行时间（毫秒）
     */
    private long executionTime;
} 