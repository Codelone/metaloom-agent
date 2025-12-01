package com.metaloom.ai.agent.nl2sql.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 表元数据模型
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableMetadata {
    /**
     * 表中文名称
     */
    @JsonProperty("表中文名称")
    private String tableNameCn;

    /**
     * 表英文名称
     */
    @JsonProperty("表英文名称")
    private String tableName;

    /**
     * 表业务定义
     */
    @JsonProperty("表业务定义")
    private String businessDefinition;

    /**
     * 字段列表
     */
    private List<FieldMetadata> fields;
}
