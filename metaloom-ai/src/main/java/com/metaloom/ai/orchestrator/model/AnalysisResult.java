package com.metaloom.ai.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据分析结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    
    /**
     * 原始查询
     */
    private String originalQuery;
    
    /**
     * 分析步骤列表
     */
    private List<AnalysisStep> steps;
    
    /**
     * 最终答案
     */
    private String finalAnswer;
    
    /**
     * 处理时间（毫秒）
     */
    private Long processingTime;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
} 