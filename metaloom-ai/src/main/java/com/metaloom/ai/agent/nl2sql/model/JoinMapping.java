package com.metaloom.ai.agent.nl2sql.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * JOIN关系映射模型
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JoinMapping {
    /**
     * 父表
     */
    @JsonProperty("父表")
    private String parentTable;

    /**
     * 父表字段
     */
    @JsonProperty("父表字段")
    private String parentField;

    /**
     * 子表
     */
    @JsonProperty("子表")
    private String childTable;

    /**
     * 子表字段
     */
    @JsonProperty("子表字段")
    private String childField;

    /**
     * 关系基数
     */
    @JsonProperty("关系基数")
    private String cardinality;
}
