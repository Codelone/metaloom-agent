package com.metaloom.ai.orchestrator;

import com.metaloom.ai.orchestrator.model.AnalysisResult;
import com.metaloom.ai.orchestrator.model.AnalysisStep;
import com.metaloom.ai.orchestrator.model.AgentAction;
import com.metaloom.ai.orchestrator.model.ActionType;
import com.metaloom.ai.orchestrator.util.LLMResponseParser;
import com.metaloom.lineage.agent.LineageAgent;
import com.metaloom.lineage.model.LineageRequest;
import com.metaloom.lineage.model.LineageResponse;
import com.metaloom.metadata.agent.MetadataAgent;
import com.metaloom.metadata.model.MetadataRequest;
import com.metaloom.metadata.model.MetadataResponse;
import com.metaloom.model.llm.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据分析智能体协调器
 * 采用A2A（Agent-to-Agent）架构，实现多智能体协作
 */
@Slf4j
@Component
public class DataAnalysisOrchestrator {

    @Autowired
    private ChatClientFactory chatClientFactory;

    @Autowired
    private LineageAgent lineageAgent;

    @Autowired
    private MetadataAgent metadataAgent;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    /**
     * 处理数据分析请求
     * 采用ReAct模式，让LLM自行决定调用哪个智能体
     *
     * @param userQuery 用户查询
     * @return 分析结果
     */
    public AnalysisResult processAnalysis(String userQuery) {
        log.info("开始处理数据分析请求: {}", userQuery);
        
        AnalysisResult result = new AnalysisResult();
        result.setOriginalQuery(userQuery);
        result.setSteps(new ArrayList<>());
        
        // 获取LLM客户端
        ChatClient llm = chatClientFactory.getClient("openai", "deepseek-v3");
        
        // 构建初始提示词
        String systemPrompt = buildSystemPrompt();
        String currentQuery = userQuery;
        int maxIterations = 5; // 最大迭代次数
        int iteration = 0;
        
        while (iteration < maxIterations) {
            iteration++;
            log.info("第{}次迭代，当前查询: {}", iteration, currentQuery);
            
            // 让LLM决定下一步行动
            String llmResponse = getLLMDecision(llm, systemPrompt, currentQuery, result);
            
            // 解析LLM响应，确定行动类型
            AgentAction action = parseLLMResponse(llmResponse);
            
            if (action == null) {
                log.warn("无法解析LLM响应: {}", llmResponse);
                break;
            }
            
            // 记录步骤
            AnalysisStep step = new AnalysisStep();
            step.setIteration(iteration);
            step.setLlmResponse(llmResponse);
            step.setAction(action);
            result.getSteps().add(step);
            
            // 执行行动
            String actionResult = executeAction(action);
            step.setActionResult(actionResult);
            
            // 检查是否已经解决问题
            if (action.getType() == ActionType.FINAL_ANSWER) {
                result.setFinalAnswer(actionResult);
                log.info("分析完成，最终答案: {}", actionResult);
                break;
            }
            
            // 更新查询，继续下一轮
            currentQuery = buildNextQuery(userQuery, actionResult, iteration);
        }
        
        if (result.getFinalAnswer() == null) {
            result.setFinalAnswer("经过多次尝试，无法完全解决您的问题。请尝试重新表述您的查询。");
        }
        
        log.info("数据分析完成，总迭代次数: {}", iteration);
        return result;
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return """
            你是一个数据分析智能体协调器。你的任务是分析用户查询，并决定调用哪个专家智能体来获取信息。
            
            可用的专家智能体：
            1. 血缘分析智能体 (lineage_agent) - 用于查询数据血缘关系、数据流向、依赖关系等
            2. 元数据查询智能体 (metadata_agent) - 用于查询表结构、字段信息、数据字典等元数据信息
            
            响应格式要求：
            请严格按照以下JSON格式响应，你可以选择以下三种行动类型之一：
            
            1. 调用单个智能体：
            {
                "action_type": "call_agent",
                "agent_name": "lineage_agent|metadata_agent",
                "request_body": {
                    "query": "具体的查询内容"
                },
                "reasoning": "解释为什么选择这个行动"
            }
            
            2. 并行执行多个任务：
            {
                "action_type": "parallel_tasks",
                "reasoning": "解释为什么需要并行执行多个任务",
                "tasks": [
                    {
                        "agent_name": "lineage_agent",
                        "description": "任务描述",
                        "request_body": {
                            "query": "具体的查询内容"
                        }
                    },
                    {
                        "agent_name": "metadata_agent",
                        "description": "任务描述",
                        "request_body": {
                            "query": "具体的查询内容"
                        }
                    }
                ]
            }
            
            3. 给出最终答案：
            {
                "action_type": "final_answer",
                "reasoning": "解释为什么已经可以给出最终答案"
            }
            
            决策规则：
            - 如果查询涉及数据流向、血缘关系、依赖关系，调用lineage_agent
            - 如果查询涉及表结构、字段信息、数据字典，调用metadata_agent
            - 如果查询同时涉及多个方面，可以使用parallel_tasks并行调用多个智能体
            - 如果已经有足够信息可以回答用户问题，使用final_answer
            - 如果无法确定，优先调用metadata_agent获取基础信息
            """;
    }

    /**
     * 获取LLM决策
     */
    private String getLLMDecision(ChatClient llm, String systemPrompt, String currentQuery, AnalysisResult result) {
        StringBuilder context = new StringBuilder();
        context.append("用户原始查询: ").append(result.getOriginalQuery()).append("\n\n");
        context.append("当前查询: ").append(currentQuery).append("\n\n");
        
        if (!result.getSteps().isEmpty()) {
            context.append("已执行的步骤:\n");
            for (AnalysisStep step : result.getSteps()) {
                context.append("- 第").append(step.getIteration()).append("步: ")
                       .append(step.getAction().getType()).append(" -> ")
                       .append(step.getActionResult()).append("\n");
            }
            context.append("\n");
        }
        
        context.append("请决定下一步行动:");
        
        Prompt prompt = new Prompt(systemPrompt + "\n\n" + context.toString());
        return llm.prompt(prompt).call().content();
    }

    /**
     * 解析LLM响应
     */
    private AgentAction parseLLMResponse(String llmResponse) {
        return LLMResponseParser.parseResponse(llmResponse);
    }

    /**
     * 执行智能体行动
     */
    private String executeAction(AgentAction action) {
        try {
            // 处理最终答案
            if (action.getType() == ActionType.FINAL_ANSWER) {
                return action.getReasoning();
            }
            
            // 处理并行任务
            if (action.getType() == ActionType.PARALLEL_TASKS && action.hasParallelTasks()) {
                return executeParallelTasks(action);
            }
            
            // 处理单个智能体调用
            switch (action.getAgentName()) {
                case "lineage_agent":
                    return executeLineageAgent(action.getRequestBody());
                case "metadata_agent":
                    return executeMetadataAgent(action.getRequestBody());
                default:
                    return "未知的智能体: " + action.getAgentName();
            }
        } catch (Exception e) {
            log.error("执行智能体行动失败: {}", action, e);
            return "执行失败: " + e.getMessage();
        }
    }
    
    /**
     * 执行并行任务
     */
    private String executeParallelTasks(AgentAction action) {
        log.info("开始执行并行任务，共{}个任务", action.getParallelTasks().size());
        
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        // 为每个任务创建一个CompletableFuture
        for (int i = 0; i < action.getParallelTasks().size(); i++) {
            final int taskIndex = i;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    var task = action.getParallelTasks().get(taskIndex);
                    String agentName = task.getAgentName();
                    String requestBody = task.getRequestBody();
                    
                    log.info("执行任务 #{}: {} - {}", taskIndex + 1, agentName, task.getDescription());
                    
                    String result;
                    long startTime = System.currentTimeMillis();
                    
                    if ("lineage_agent".equals(agentName)) {
                        result = executeLineageAgent(requestBody);
                    } else if ("metadata_agent".equals(agentName)) {
                        result = executeMetadataAgent(requestBody);
                    } else {
                        result = "未知的智能体: " + agentName;
                    }
                    
                    long executionTime = System.currentTimeMillis() - startTime;
                    task.setExecutionTime(executionTime);
                    task.setResult(result);
                    
                    return String.format("任务 #%d (%s): %s", taskIndex + 1, agentName, result);
                } catch (Exception e) {
                    log.error("任务执行失败", e);
                    return "任务 #" + (taskIndex + 1) + " 执行失败: " + e.getMessage();
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // 等待所有任务完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        try {
            allOf.join();
            
            // 合并所有结果
            StringBuilder result = new StringBuilder();
            result.append("并行任务执行结果:\n\n");
            
            for (int i = 0; i < action.getParallelTasks().size(); i++) {
                var task = action.getParallelTasks().get(i);
                String taskResult = futures.get(i).get();
                
                result.append("任务 #").append(i + 1)
                      .append(" (").append(task.getAgentName()).append("): ")
                      .append(task.getDescription())
                      .append("\n执行时间: ").append(task.getExecutionTime()).append("ms\n")
                      .append("结果: ").append(task.getResult())
                      .append("\n\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("等待并行任务完成时发生错误", e);
            return "并行任务执行失败: " + e.getMessage();
        }
    }

    /**
     * 执行血缘分析智能体
     */
    private String executeLineageAgent(String query) {
        LineageRequest request = LineageRequest.builder()
            .query(query)
            .depth(2)
            .build();
        
        LineageResponse response = lineageAgent.processQuery(request);
        return response.toString();
    }

    /**
     * 执行元数据查询智能体
     */
    private String executeMetadataAgent(String query) {
        MetadataRequest request = MetadataRequest.builder()
            .query(query)
            .build();
        
        MetadataResponse response = metadataAgent.processQuery(request);
        return response.toString();
    }

    /**
     * 构建下一轮查询
     */
    private String buildNextQuery(String originalQuery, String actionResult, int iteration) {
        return String.format("基于第%d步的结果: %s，继续分析原始问题: %s", 
            iteration, actionResult, originalQuery);
    }

    /**
     * 异步处理多个智能体调用
     */
    public AnalysisResult processAnalysisAsync(String userQuery) throws ExecutionException, InterruptedException {
        log.info("开始异步处理数据分析请求: {}", userQuery);
        
        AnalysisResult result = new AnalysisResult();
        result.setOriginalQuery(userQuery);
        result.setSteps(new ArrayList<>());
        
        // 并行调用两个智能体获取基础信息
        CompletableFuture<String> lineageFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return executeLineageAgent(userQuery);
            } catch (Exception e) {
                log.error("血缘分析失败", e);
                return "血缘分析失败: " + e.getMessage();
            }
        }, executorService);
        
        CompletableFuture<String> metadataFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return executeMetadataAgent(userQuery);
            } catch (Exception e) {
                log.error("元数据查询失败", e);
                return "元数据查询失败: " + e.getMessage();
            }
        }, executorService);
        
        // 等待所有结果
        CompletableFuture.allOf(lineageFuture, metadataFuture).join();
        
        // 合并结果
        String lineageResult = lineageFuture.get();
        String metadataResult = metadataFuture.get();
        
        // 使用LLM整合结果
        ChatClient llm = chatClientFactory.getClient("openai", "deepseek-v3");
        String integratedResult = integrateResults(llm, userQuery, lineageResult, metadataResult);
        
        result.setFinalAnswer(integratedResult);
        log.info("异步分析完成");
        
        return result;
    }

    /**
     * 整合多个智能体的结果
     */
    private String integrateResults(ChatClient llm, String originalQuery, String lineageResult, String metadataResult) {
        String prompt = String.format("""
            请基于以下信息回答用户的问题：
            
            用户问题：%s
            
            血缘分析结果：%s
            
            元数据查询结果：%s
            
            请整合这些信息，给出完整、准确的答案。
            """, originalQuery, lineageResult, metadataResult);
        
        Prompt promptObj = new Prompt(prompt);
        return llm.prompt(promptObj).call().content();
    }
} 