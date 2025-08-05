package com.metaloom.ai.orchestrator;

import com.metaloom.ai.orchestrator.model.AnalysisResult;
import com.metaloom.ai.orchestrator.util.LLMResponseParser;
import com.metaloom.ai.orchestrator.model.AgentAction;
import com.metaloom.ai.orchestrator.model.ActionType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A2A智能体协调器测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class DataAnalysisOrchestratorTest {

    @Test
    public void testLLMResponseParser() {
        // 测试血缘分析智能体响应解析
        String lineageResponse = """
            {
                "action_type": "call_agent",
                "agent_name": "lineage_agent",
                "request_body": {
                    "query": "查询用户表的数据血缘关系"
                },
                "reasoning": "用户查询涉及数据血缘关系，应该调用血缘分析智能体"
            }
            """;
        
        AgentAction lineageAction = LLMResponseParser.parseResponse(lineageResponse);
        assertNotNull(lineageAction);
        assertEquals(ActionType.CALL_AGENT, lineageAction.getType());
        assertEquals("lineage_agent", lineageAction.getAgentName());
        assertEquals("查询用户表的数据血缘关系", lineageAction.getRequestBody());
        assertTrue(lineageAction.getReasoning().contains("血缘关系"));
        
        // 测试元数据查询智能体响应解析
        String metadataResponse = """
            {
                "action_type": "call_agent",
                "agent_name": "metadata_agent",
                "request_body": {
                    "query": "查询用户表的字段结构"
                },
                "reasoning": "用户查询涉及表结构信息，应该调用元数据查询智能体"
            }
            """;
        
        AgentAction metadataAction = LLMResponseParser.parseResponse(metadataResponse);
        assertNotNull(metadataAction);
        assertEquals(ActionType.CALL_AGENT, metadataAction.getType());
        assertEquals("metadata_agent", metadataAction.getAgentName());
        assertEquals("查询用户表的字段结构", metadataAction.getRequestBody());
        assertTrue(metadataAction.getReasoning().contains("表结构"));
        
        // 测试最终答案响应解析
        String finalAnswerResponse = """
            {
                "action_type": "final_answer",
                "reasoning": "已经收集到足够的信息，可以给出最终答案"
            }
            """;
        
        AgentAction finalAction = LLMResponseParser.parseResponse(finalAnswerResponse);
        assertNotNull(finalAction);
        assertEquals(ActionType.FINAL_ANSWER, finalAction.getType());
        assertTrue(finalAction.getReasoning().contains("最终答案"));
        
        // 测试并行任务响应解析
        String parallelTasksResponse = """
            {
                "action_type": "parallel_tasks",
                "reasoning": "需要同时获取血缘关系和元数据信息",
                "tasks": [
                    {
                        "agent_name": "lineage_agent",
                        "description": "查询用户表的血缘关系",
                        "request_body": {
                            "query": "查询用户表的数据血缘关系"
                        }
                    },
                    {
                        "agent_name": "metadata_agent",
                        "description": "查询用户表的元数据",
                        "request_body": {
                            "query": "查询用户表的字段结构"
                        }
                    }
                ]
            }
            """;
        
        AgentAction parallelAction = LLMResponseParser.parseResponse(parallelTasksResponse);
        assertNotNull(parallelAction);
        assertEquals(ActionType.PARALLEL_TASKS, parallelAction.getType());
        assertTrue(parallelAction.getReasoning().contains("同时获取"));
        assertEquals(2, parallelAction.getParallelTasks().size());
        
        // 验证第一个任务
        assertEquals("lineage_agent", parallelAction.getParallelTasks().get(0).getAgentName());
        assertEquals("查询用户表的血缘关系", parallelAction.getParallelTasks().get(0).getDescription());
        assertEquals("查询用户表的数据血缘关系", parallelAction.getParallelTasks().get(0).getRequestBody());
        
        // 验证第二个任务
        assertEquals("metadata_agent", parallelAction.getParallelTasks().get(1).getAgentName());
        assertEquals("查询用户表的元数据", parallelAction.getParallelTasks().get(1).getDescription());
        assertEquals("查询用户表的字段结构", parallelAction.getParallelTasks().get(1).getRequestBody());
    }
    
    @Test
    public void testInvalidResponse() {
        // 测试无效响应
        String invalidResponse = "这是一个无效的响应";
        AgentAction action = LLMResponseParser.parseResponse(invalidResponse);
        assertNull(action);
        
        // 测试格式错误的JSON
        String malformedJson = "{ invalid json }";
        AgentAction malformedAction = LLMResponseParser.parseResponse(malformedJson);
        assertNull(malformedAction);
    }
    
    @Test
    public void testResponseValidation() {
        // 测试有效响应
        String validResponse = """
            {
                "action_type": "call_agent",
                "agent_name": "lineage_agent",
                "request_body": {
                    "query": "测试查询"
                },
                "reasoning": "测试推理"
            }
            """;
        
        assertTrue(LLMResponseParser.isValidResponse(validResponse));
        
        // 测试无效响应
        String invalidResponse = "无效响应";
        assertFalse(LLMResponseParser.isValidResponse(invalidResponse));
    }
} 