package com.metaloom.ai.orchestrator.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaloom.ai.orchestrator.model.AgentAction;
import com.metaloom.ai.orchestrator.model.AgentTask;
import com.metaloom.ai.orchestrator.model.ActionType;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * LLM响应解析器
 * 用于解析LLM的JSON格式响应
 */
@Slf4j
public class LLMResponseParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析LLM响应为智能体行动
     *
     * @param llmResponse LLM响应
     * @return 智能体行动
     */
    public static AgentAction parseResponse(String llmResponse) {
        try {
            // 尝试提取JSON部分
            String jsonPart = extractJsonPart(llmResponse);
            if (jsonPart == null) {
                log.warn("无法从响应中提取JSON: {}", llmResponse);
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(jsonPart);
            
            String actionType = getStringValue(jsonNode, "action_type");
            String reasoning = getStringValue(jsonNode, "reasoning");
            
            // 根据action_type确定行动类型
            ActionType type;
            if ("final_answer".equals(actionType)) {
                type = ActionType.FINAL_ANSWER;
            } else if ("parallel_tasks".equals(actionType)) {
                type = ActionType.PARALLEL_TASKS;
            } else {
                type = ActionType.CALL_AGENT;
            }
            
            AgentAction.AgentActionBuilder builder = AgentAction.builder()
                .type(type)
                .reasoning(reasoning);
            
            // 如果是单个智能体调用
            if (type == ActionType.CALL_AGENT) {
                String agentName = getStringValue(jsonNode, "agent_name");
                String requestBody = extractRequestBody(jsonNode);
                
                builder.agentName(agentName)
                      .requestBody(requestBody);
            }
            // 如果是并行任务
            else if (type == ActionType.PARALLEL_TASKS && jsonNode.has("tasks") && jsonNode.get("tasks").isArray()) {
                List<AgentTask> tasks = new ArrayList<>();
                JsonNode tasksNode = jsonNode.get("tasks");
                
                for (JsonNode taskNode : tasksNode) {
                    String agentName = getStringValue(taskNode, "agent_name");
                    String description = getStringValue(taskNode, "description");
                    String requestBody = extractRequestBody(taskNode);
                    
                    tasks.add(AgentTask.builder()
                        .agentName(agentName)
                        .description(description)
                        .requestBody(requestBody)
                        .build());
                }
                
                builder.parallelTasks(tasks);
            }
            
            return builder.build();

        } catch (Exception e) {
            log.error("解析LLM响应失败: {}", llmResponse, e);
            return null;
        }
    }
    
    /**
     * 从节点中提取请求体
     */
    private static String extractRequestBody(JsonNode node) {
        String requestBody = "";
        if (node.has("request_body")) {
            JsonNode requestBodyNode = node.get("request_body");
            if (requestBodyNode.has("query")) {
                requestBody = requestBodyNode.get("query").asText();
            } else if (requestBodyNode.isTextual()) {
                requestBody = requestBodyNode.asText();
            } else {
                requestBody = requestBodyNode.toString();
            }
        }
        return requestBody;
    }

    /**
     * 从响应中提取JSON部分
     */
    private static String extractJsonPart(String response) {
        // 查找JSON开始和结束位置
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        
        if (start == -1 || end == -1 || start >= end) {
            return null;
        }
        
        return response.substring(start, end + 1);
    }

    /**
     * 安全获取字符串值
     */
    private static String getStringValue(JsonNode node, String fieldName) {
        if (node.has(fieldName)) {
            JsonNode fieldNode = node.get(fieldName);
            return fieldNode.isTextual() ? fieldNode.asText() : fieldNode.toString();
        }
        return "";
    }

    /**
     * 验证响应格式是否正确
     */
    public static boolean isValidResponse(String llmResponse) {
        try {
            AgentAction action = parseResponse(llmResponse);
            return action != null && action.getType() != null;
        } catch (Exception e) {
            return false;
        }
    }
} 