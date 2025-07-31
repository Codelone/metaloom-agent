package com.metaloom.lineage.agent;

import com.metaloom.client.tools.ToolCallbackProviderService;
import com.metaloom.lineage.model.LineageRequest;
import com.metaloom.lineage.model.LineageResponse;
import com.metaloom.model.llm.ChatClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
            你是一个数据血缘关系分析专家，负责帮助用户查询和分析数据表和字段之间的血缘关系。请遵循以下ReAct过程：
            
            1. 思考(Reasoning): 分析用户查询的意图，确定需要查询的表ID或字段ID，以及需要查询的血缘关系类型（上游或下游）和查询深度。
            2. 行动(Acting): 调用相应的血缘查询工具获取数据。
            3. 观察(Observing): 分析工具返回的结果，进行归纳总结或继续深入查询。
            
            你可以使用以下工具:
            - queryUpstreamLineage: 查询元数据的上游血缘关系（数据来源）
            - queryDownstreamLineage: 查询元数据的下游血缘关系（数据去向）
            - queryFullLineage: 查询元数据的完整血缘关系图
            
            对于深度血缘查询，你需要递归调用血缘查询工具，直到达到用户指定的深度或无法继续查询为止。
            
            返回结果应该以清晰、规整的格式呈现，包括：
            1. 血缘关系的层次结构
            2. 每个节点的详细信息
            3. 节点之间的关系说明
            4. 完整的血缘路径分析
            
            请确保你的回答准确、详细且易于理解。如果信息不足，请明确指出并要求用户提供更多信息。
            """;

    /**
     * 处理用户血缘查询请求
     * 
     * @param request 血缘查询请求
     * @return 血缘查询响应
     */
    public LineageResponse processQuery(LineageRequest request) {
        ChatClient openai = chatClientFactory.getClient("openai", "deepseek-v3");
        // 提取请求中的ID
        String instId = extractInstId(request.getQuery());
        if (instId == null || instId.isEmpty()) {
            return LineageResponse.builder()
                    .success(false)
                    .message("未能识别有效的实例ID，请提供正确的表ID或字段ID")
                    .build();
        }

        // 创建提示并使用血缘工具
        var promptBuilder = openai
                .prompt()
                .system(SYSTEM_PROMPT) // 系统提示词
                .user(request.getQuery())  // 用户查询
                .tools(toolCallbackProviderService.getLineageToolCallbackProvider());
        // 执行ReAct过程
        String response = promptBuilder.call().content();
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