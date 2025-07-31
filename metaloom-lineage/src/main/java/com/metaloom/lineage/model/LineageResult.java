package com.metaloom.lineage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 血缘查询结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageResult {
    /**
     * 查询是否成功
     */
    private boolean success;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 血缘节点列表
     */
    private List<LineageNode> nodes;
    
    /**
     * 血缘路径列表
     */
    private List<List<LineageNode>> paths;
    
    /**
     * 总节点数量
     */
    private int totalCount;
} 