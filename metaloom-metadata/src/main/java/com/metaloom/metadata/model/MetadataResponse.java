package com.metaloom.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 元数据响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataResponse {
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
     * 关键词
     */
    private String keyword;
} 
