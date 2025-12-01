package com.metaloom.ai.agent.nl2sql.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaloom.ai.agent.nl2sql.model.TableMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表元数据服务
 * 负责加载和提供表元数据信息
 */
@Slf4j
@Service
public class TableMetadataService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<TableMetadata> allTables;
    private Map<String, TableMetadata> tableMap;

    @PostConstruct
    public void initialize() {
        loadMetadata();
    }

    /**
     * 加载表元数据
     */
    private void loadMetadata() {
        try {
            log.info("开始加载表元数据...");
            ClassPathResource resource = new ClassPathResource("adm_pub_table.json");

            try (InputStream inputStream = resource.getInputStream()) {
                allTables = objectMapper.readValue(inputStream, new TypeReference<List<TableMetadata>>() {
                });

                // 构建索引map（表英文名 -> TableMetadata）
                tableMap = allTables.stream()
                        .collect(Collectors.toMap(
                                TableMetadata::getTableName,
                                table -> table,
                                (existing, replacement) -> existing // 处理重复key
                        ));

                log.info("表元数据加载完成，共 {} 张表", allTables.size());
            }
        } catch (IOException e) {
            log.error("加载表元数据失败", e);
            throw new RuntimeException("无法加载表元数据", e);
        }
    }

    /**
     * 获取所有表的简要信息（用于LLM提示词）
     */
    public String getTablesSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("可用的数据表列表:\n\n");

        for (TableMetadata table : allTables) {
            summary.append(String.format("- 表名: %s\n", table.getTableName()));
            summary.append(String.format("  中文名: %s\n", table.getTableNameCn()));
            summary.append(String.format("  说明: %s\n", table.getBusinessDefinition()));
            summary.append("\n");
        }

        return summary.toString();
    }

    /**
     * 根据表英文名获取完整的表结构
     */
    public TableMetadata getTableSchema(String tableName) {
        TableMetadata table = tableMap.get(tableName);
        if (table == null) {
            log.warn("未找到表: {}", tableName);
        }
        return table;
    }

    /**
     * 获取所有表
     */
    public List<TableMetadata> getAllTables() {
        return allTables;
    }
}
