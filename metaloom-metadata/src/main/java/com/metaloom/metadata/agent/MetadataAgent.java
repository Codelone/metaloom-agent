package com.metaloom.metadata.agent;

import com.metaloom.client.tools.ToolCallbackProviderService;
import com.metaloom.metadata.model.MetadataRequest;
import com.metaloom.metadata.model.MetadataResponse;
import com.metaloom.model.llm.ChatClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 元数据智能体
 */
@Service
public class MetadataAgent {

    @Autowired
    private ChatClientFactory chatClientFactory;

    @Autowired
    private ToolCallbackProviderService toolCallbackProviderService;

    private static final String SYSTEM_PROMPT = """
            你是一个元数据查询专家，负责帮助用户查询和分析元数据列表和元数据详情。
            
            你可以使用以下工具:
            - metadataListTool: 根据关键字搜索匹配的元数据列表
            - metadataDetailTool: 根据instId列表查询元数据的详细属性信息
            
            重要提示：
            1. 根据用户提供的关键字，搜索匹配的元数据列表，然后选取最匹配用户关键字的元数据，将该元数据的详细属性信息返回给用户
            2. 请根据用户查询内容智能选择合适的工具组合
            3. 最终以结构化和用户易读的格式展示查询结果
            """;

    /**
     * 处理元数据查询请求
     *
     * @param request 元数据请求
     * @return 元数据响应
     */
    public MetadataResponse processQuery(MetadataRequest request) {
        ChatClient chatClient = chatClientFactory.getClient("ollama", "qwen");

        // 提取请求中的关键字
        String keyword = extractKeyword(request.getQuery());

        if (keyword == null || keyword.isEmpty()) {
            return MetadataResponse.builder()
                    .success(false)
                    .message("未能识别有效的查询关键字，请提供正确的表名、字段名或相关描述")
                    .build();
        }

        // 添加初始用户查询
        String currentUserMessage = request.getQuery();

        System.out.println("用户消息: " + currentUserMessage);

        // 使用Spring AI 1.0的正确语法调用模型
        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(currentUserMessage)
                .toolCallbacks(toolCallbackProviderService.getMetadataToolCallbackProvider())
                .call()
                .content();

        System.out.println("模型响应: " + response);

        // 处理并格式化响应
        return MetadataResponse.builder()
                .success(true)
                .message("元数据查询成功")
                .result(response)
                .keyword(keyword)
                .build();
    }

    /**
     * 从查询中提取关键字
     */
    private String extractKeyword(String query) {
        // 如果查询为空，返回null
        if (query == null || query.trim().isEmpty()) {
            return null;
        }
        
        String trimmedQuery = query.trim();
        
        // 1. 匹配引号内的内容（如"ads_tv"、"用户表"等）
        Pattern quotePattern = Pattern.compile("[\"']([^\"']+)[\"']");
        Matcher quoteMatcher = quotePattern.matcher(trimmedQuery);
        if (quoteMatcher.find()) {
            return quoteMatcher.group(1);
        }
        
        // 2. 匹配"查询xxx"、"搜索xxx"、"查找xxx"等模式
        Pattern searchPattern = Pattern.compile("(?:查询|搜索|查找|找|查)\\s*([\\w\\u4e00-\\u9fa5_-]+)");
        Matcher searchMatcher = searchPattern.matcher(trimmedQuery);
        if (searchMatcher.find()) {
            return searchMatcher.group(1);
        }
        
        // 3. 匹配"表xxx"、"字段xxx"、"元数据xxx"等模式
        Pattern metadataPattern = Pattern.compile("(?:表|字段|元数据|数据)\\s*([\\w\\u4e00-\\u9fa5_-]+)");
        Matcher metadataMatcher = metadataPattern.matcher(trimmedQuery);
        if (metadataMatcher.find()) {
            return metadataMatcher.group(1);
        }
        
        // 4. 匹配"关于xxx"、"xxx的信息"、"xxx元数据"等模式
        Pattern infoPattern = Pattern.compile("(?:关于|的信息|详情|元数据)\\s*([\\w\\u4e00-\\u9fa5_-]+)");
        Matcher infoMatcher = infoPattern.matcher(trimmedQuery);
        if (infoMatcher.find()) {
            return infoMatcher.group(1);
        }
        
        // 5. 如果查询长度较短（<=20字符），直接返回整个查询作为关键字
        if (trimmedQuery.length() <= 20) {
            return trimmedQuery;
        }
        
        // 6. 提取查询中的第一个有意义的关键词（排除常见停用词）
        String[] stopWords = {"请", "帮我", "查询", "搜索", "查找", "找", "查", "关于", "的", "信息", "详情", "表", "字段", "元数据", "数据"};
        String[] words = trimmedQuery.split("[\\s\\p{Punct}]+");
        
        for (String word : words) {
            if (word.length() > 1 && !isStopWord(word, stopWords)) {
                return word;
            }
        }
        
        return null;
    }
    
    /**
     * 判断是否为停用词
     */
    private boolean isStopWord(String word, String[] stopWords) {
        for (String stopWord : stopWords) {
            if (stopWord.equals(word)) {
                return true;
            }
        }
        return false;
    }
} 
