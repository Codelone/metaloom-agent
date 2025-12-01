package com.metaloom.ai.agent.nl2sql.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 字段元数据模型
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldMetadata {
    /**
     * 字段中文名称
     */
    @JsonProperty("字段中文名称")
    private String fieldNameCn;

    /**
     * 字段英文名称
     */
    @JsonProperty("字段英文名称")
    private String fieldName;

    /**
     * 是否主键
     */
    @JsonProperty("是否主键")
    private String isPrimaryKey;

    /**
     * 字段类型
     */
    @JsonProperty("字段类型")
    private String fieldType;

    /**
     * 业务定义
     */
    @JsonProperty("业务定义")
    private String businessDefinition;

    /**
     * 是否代码字段
     */
    @JsonProperty("是否代码字段")
    private String isCodeField;

    /**
     * 字段码值
     */
    @JsonProperty("字段码值")
    private String codeValue;
}
