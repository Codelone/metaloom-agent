package com.metaloom.ai.agent.nl2sql.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaloom.ai.agent.nl2sql.model.JoinMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JOIN关系映射服务
 * 负责加载和提供表JOIN关系信息
 */
@Slf4j
@Service
public class JoinMappingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<JoinMapping> allMappings;

    @PostConstruct
    public void initialize() {
        loadMappings();
    }

    /**
     * 加载JOIN映射数据
     */
    private void loadMappings() {
        try {
            log.info("开始加载JOIN映射数据...");
            ClassPathResource resource = new ClassPathResource("adm_pub_field_mapping.json");

            try (InputStream inputStream = resource.getInputStream()) {
                allMappings = objectMapper.readValue(inputStream, new TypeReference<List<JoinMapping>>() {
                });
                log.info("JOIN映射数据加载完成，共 {} 条关系", allMappings.size());
            }
        } catch (IOException e) {
            log.error("加载JOIN映射数据失败", e);
            throw new RuntimeException("无法加载JOIN映射数据", e);
        }
    }

    /**
     * 获取指定表之间的JOIN关系
     * 
     * @param tableNames 表名列表
     * @return JOIN关系列表
     */
    public List<JoinMapping> getJoinRelations(List<String> tableNames) {
        if (tableNames == null || tableNames.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为大写以匹配
        List<String> upperTableNames = tableNames.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        return allMappings.stream()
                .filter(mapping -> {
                    String parent = mapping.getParentTable().toUpperCase();
                    String child = mapping.getChildTable().toUpperCase();
                    return upperTableNames.contains(parent) && upperTableNames.contains(child);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取所有JOIN关系
     */
    public List<JoinMapping> getAllMappings() {
        return allMappings;
    }
}
