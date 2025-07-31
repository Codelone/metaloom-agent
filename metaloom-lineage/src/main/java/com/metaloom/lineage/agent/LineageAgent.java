package com.metaloom.lineage.agent;

import com.metaloom.client.tools.ToolCallbackProviderService;
import com.metaloom.lineage.model.LineageRequest;
import com.metaloom.lineage.model.LineageResponse;
import com.metaloom.model.llm.ChatClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据血缘智能体，基于ReAct模式实现深度血缘查询
 */
@Service
public class LineageAgent {

    @Autowired
    private ChatClientFactory chatClientFactory;

    @Autowired
    private ToolCallbackProviderService toolCallbackProviderService;

    private static final String SYSTEM_PROMPT = """
            你是一个数据血缘关系分析专家，负责帮助用户查询和分析数据表和字段之间的血缘关系。
            
            你可以使用以下工具:
            - queryUpstreamLineage: 查询元数据的上游血缘关系（数据来源）
            - queryDownstreamLineage: 查询元数据的下游血缘关系（数据去向）
            - queryFullLineage: 查询元数据的完整血缘关系图
            
            重要提示：
            1. 每个工具调用只返回一层的血缘关系
            2. 要获取多层血缘关系，你需要对返回结果中的每个节点ID继续调用相应工具
            3. 请根据用户需求确定查询深度，并递归调用工具直到达到指定深度
            4. 最终请以层次化、清晰的格式展示完整的血缘关系树
            """;
//    当你完成了所有必要的工具调用后，请在回答最后加上"[COMPLETE]"标记。

    /**
     * 处理用户血缘查询请求
     *
     * @param request 血缘查询请求
     * @return 血缘查询响应
     */
    public LineageResponse processQuery(LineageRequest request) {
        ChatClient chatClient = chatClientFactory.getClient("openai", "gpt-3");

        // 提取请求中的ID
        String instId = extractInstId(request.getQuery());

        if (instId == null || instId.isEmpty()) {
            return LineageResponse.builder()
                    .success(false)
                    .message("未能识别有效的实例ID，请提供正确的表ID或字段ID")
                    .build();
        }

        // 添加初始用户查询
        String currentUserMessage = request.getQuery();

        System.out.println("用户消息: " + currentUserMessage);

        // 使用Spring AI 1.0的正确语法调用模型
        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(currentUserMessage)
                .toolCallbacks(toolCallbackProviderService.getLineageToolCallbackProvider())
                .call()
                .content();

        System.out.println("模型响应: " + response);

        int queryDepth = request.getDepth() > 0 ? request.getDepth() : 1;

        // 处理并格式化响应
        return LineageResponse.builder()
                .success(true)
                .message("血缘查询成功")
                .result(response)
                .instId(instId)
                .depth(queryDepth)
                .build();
    }

    /**
     * 从查询中提取实例ID
     */
    private String extractInstId(String query) {
        // 使用正则表达式提取ID
        // 匹配类似"ID为xxx"、"id:xxx"、"表xxx"等模式
        Pattern pattern = Pattern.compile("(?:[iI][dD][为是:=\\s]+|[表字段][名为]?[为是:=\\s]+|[表字段]\\s*[：:])\\s*([\\w.-]+)");
        Matcher matcher = pattern.matcher(query);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // 尝试匹配单独的ID格式（如UUID或特定格式的ID）
        Pattern idPattern = Pattern.compile("\\b([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\\b",
                Pattern.CASE_INSENSITIVE);
        Matcher idMatcher = idPattern.matcher(query);

        if (idMatcher.find()) {
            return idMatcher.group(1);
        }

        return null;
    }
} 