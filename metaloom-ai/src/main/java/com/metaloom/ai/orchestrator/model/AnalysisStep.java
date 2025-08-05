package com.metaloom.ai.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分析步骤
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisStep {
    
    /**
     * 步骤序号
     */
    private Integer iteration;
    
    /**
     * LLM响应
     */
    private String llmResponse;
    
    /**
     * 智能体行动
     */
    private AgentAction action;
    
    /**
     * 行动结果
     */
    private String actionResult;
    
    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;
} 