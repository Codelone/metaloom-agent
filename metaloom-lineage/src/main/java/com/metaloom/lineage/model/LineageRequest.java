package com.metaloom.lineage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 血缘查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageRequest {
    /**
     * 查询文本
     */
    private String query;
    
    /**
     * 查询深度，默认为1
     */
    private int depth = 1;
} 