package com.metaloom.lineage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 血缘关系节点
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageNode {
    /**
     * 节点ID
     */
    private String id;
    
    /**
     * 节点名称
     */
    private String name;
    
    /**
     * 节点类型，如表、字段等
     */
    private String type;
    
    /**
     * 节点属性
     */
    private Map<String, String> properties;
} 