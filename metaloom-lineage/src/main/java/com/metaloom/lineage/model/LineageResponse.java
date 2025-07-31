package com.metaloom.lineage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 血缘查询响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageResponse {
    /**
     * 查询是否成功
     */
    private boolean success;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 查询结果文本
     */
    private String result;
    
    /**
     * 实例ID
     */
    private String instId;
    
    /**
     * 查询深度
     */
    private int depth;
} 