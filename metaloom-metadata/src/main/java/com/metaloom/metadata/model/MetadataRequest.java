package com.metaloom.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 元数据请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataRequest {
    /**
     * 查询文本
     */
    private String query;
} 
