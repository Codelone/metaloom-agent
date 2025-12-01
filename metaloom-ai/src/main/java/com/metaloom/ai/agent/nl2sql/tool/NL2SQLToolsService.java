package com.metaloom.ai.agent.nl2sql.tool;

import com.metaloom.ai.agent.nl2sql.model.JoinMapping;
import com.metaloom.ai.agent.nl2sql.model.TableMetadata;
import com.metaloom.ai.agent.nl2sql.service.JoinMappingService;
import com.metaloom.ai.agent.nl2sql.service.TableMetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * NL2SQL工具函数服务
 * 使用Spring AI @Tool注解定义工具
 */
@Slf4j
@Service
public class NL2SQLToolsService {

    private final TableMetadataService tableMetadataService;
    private final JoinMappingService joinMappingService;

    @Autowired
    public NL2SQLToolsService(TableMetadataService tableMetadataService,
            JoinMappingService joinMappingService) {
        this.tableMetadataService = tableMetadataService;
        this.joinMappingService = joinMappingService;
    }

    public record TableSchemaResponse(
            String tableName,
            String tableNameCn,
            String businessDefinition,
            List<FieldInfo> fields,
            String errorMessage) {
    }

    public record FieldInfo(
            String fieldName,
            String fieldNameCn,
            String fieldType,
            boolean isPrimaryKey,
            String businessDefinition,
            String codeValue) {
    }

    @Tool(description = "获取指定表的完整字段Schema信息，包括字段名、类型、业务定义等")
    public TableSchemaResponse getTableSchema(String tableName) {
        log.info("Tool调用: getTableSchema, tableName={}", tableName);

        TableMetadata table = tableMetadataService.getTableSchema(tableName);
        if (table == null) {
            log.warn("未找到表: {}", tableName);
            return new TableSchemaResponse(
                    tableName,
                    "",
                    "",
                    null,
                    "未找到表" + tableName);
        }

        List<FieldInfo> fields = table.getFields().stream()
                .map(field -> new FieldInfo(
                        field.getFieldName(),
                        field.getFieldNameCn(),
                        field.getFieldType(),
                        "是".equals(field.getIsPrimaryKey()),
                        field.getBusinessDefinition(),
                        field.getCodeValue()))
                .collect(Collectors.toList());

        return new TableSchemaResponse(
                table.getTableName(),
                table.getTableNameCn(),
                table.getBusinessDefinition(),
                fields,
                "");
    }

    public record JoinRelationResponse(
            String parentTable,
            String parentField,
            String childTable,
            String childField,
            String cardinality) {
    }

    @Tool(description = "获取多张表之间的JOIN关系，返回父表、子表、关联字段和关系基数")
    public List<JoinRelationResponse> getJoinRelations(List<String> tableNames) {
        log.info("Tool调用: getJoinRelations, tableNames={}", tableNames);

        List<JoinMapping> mappings = joinMappingService.getJoinRelations(tableNames);

        return mappings.stream()
                .map(mapping -> new JoinRelationResponse(
                        mapping.getParentTable(),
                        mapping.getParentField(),
                        mapping.getChildTable(),
                        mapping.getChildField(),
                        mapping.getCardinality()))
                .collect(Collectors.toList());
    }
}
