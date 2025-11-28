package com.metaloom.metadata.agent;

import com.metaloom.client.tools.ToolCallbackProviderService;
import com.metaloom.metadata.model.MetadataRequest;
import com.metaloom.metadata.model.MetadataResponse;
import com.metaloom.model.llm.ChatClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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

    @Value("${ai.active}")
    private String modelName;

    private static final String SYSTEM_PROMPT = """
            你是一个元数据查询专家，负责帮助用户查询和分析元数据列表和元数据详情。
            元数据说明：
            元数据类型分为：OdpsTable-表、OdpsColumn-字段
            每个元数据都具有唯一标识 instId
            
            你可以使用以下工具:
            - metadataListTool: 根据关键字模糊搜索匹配的元数据列表
            - metadataDetailTool: 根据instId列表查询元数据的详细属性信息
            - queryTableColumnMetadata: 根据instId查询一张表的下级字段信息
            
            重要提示：
            1. 用户查询时，若查询不包含instId，需要先使用metadataListTool查询可能的元数据列表，并选取匹配一致的结果作为用户查询的元数据，再使用instId查询具体元数据详细信息。
            2. 用户查询包含instId，则直接调用metadataDetailTool获取元数据信息。
            3. 请根据用户查询内容智能选择合适的工具组合完成查询。
            4. 输出结果除用户要求的以外，需包含中文名、英文名、instId。
            5. 最终以结构化的格式展示查询结果。
            """;
//               若没有完全一致的元数据，则返回所有可能的结果请用户进一步判断。
    /**
     * 处理元数据查询请求（流式输出）
     *
     * @param request 元数据请求
     * @return 模型输出的流
     */
    public Flux<String> processQuery(MetadataRequest request) {
        ChatClient chatClient = chatClientFactory.getClient("openai", modelName);

        // 添加初始用户查询
        String currentUserMessage = request.getQuery();

        System.out.println("metadata用户消息: " + currentUserMessage);

        // 使用Spring AI 1.0的流式API
        Flux<String> stream = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(currentUserMessage)
                .toolCallbacks(toolCallbackProviderService.getMetadataToolCallbackProvider())
                .stream()
                .content();

        return stream;
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
