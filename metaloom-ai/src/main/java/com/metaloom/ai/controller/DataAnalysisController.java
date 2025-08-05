package com.metaloom.ai.controller;

import com.metaloom.ai.orchestrator.DataAnalysisOrchestrator;
import com.metaloom.ai.orchestrator.model.AnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据分析智能体控制器
 * 提供A2A架构的智能体协调服务
 */
@Slf4j
@RestController
@RequestMapping("/api/analysis")
public class DataAnalysisController {

    @Autowired
    private DataAnalysisOrchestrator orchestrator;

    /**
     * 处理数据分析请求（ReAct模式）
     *
     * @param request 分析请求
     * @return 分析结果
     */
    @PostMapping("/react")
    public ResponseEntity<AnalysisResult> processAnalysisReact(@RequestBody AnalysisRequest request) {
        log.info("收到ReAct模式数据分析请求: {}", request.getQuery());
        
        long startTime = System.currentTimeMillis();
        
        try {
            AnalysisResult result = orchestrator.processAnalysis(request.getQuery());
            result.setProcessingTime(System.currentTimeMillis() - startTime);
            result.setSuccess(true);
            
            log.info("ReAct模式分析完成，耗时: {}ms", result.getProcessingTime());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("ReAct模式分析失败", e);
            
            AnalysisResult errorResult = AnalysisResult.builder()
                .originalQuery(request.getQuery())
                .success(false)
                .errorMessage(e.getMessage())
                .processingTime(System.currentTimeMillis() - startTime)
                .build();
            
            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * 处理数据分析请求（异步模式）
     *
     * @param request 分析请求
     * @return 分析结果
     */
    @PostMapping("/async")
    public ResponseEntity<AnalysisResult> processAnalysisAsync(@RequestBody AnalysisRequest request) {
        log.info("收到异步模式数据分析请求: {}", request.getQuery());
        
        long startTime = System.currentTimeMillis();
        
        try {
            AnalysisResult result = orchestrator.processAnalysisAsync(request.getQuery());
            result.setProcessingTime(System.currentTimeMillis() - startTime);
            result.setSuccess(true);
            
            log.info("异步模式分析完成，耗时: {}ms", result.getProcessingTime());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("异步模式分析失败", e);
            
            AnalysisResult errorResult = AnalysisResult.builder()
                .originalQuery(request.getQuery())
                .success(false)
                .errorMessage(e.getMessage())
                .processingTime(System.currentTimeMillis() - startTime)
                .build();
            
            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * 获取智能体状态信息
     *
     * @return 状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", LocalDateTime.now());
        status.put("service", "Data Analysis A2A Orchestrator");
        status.put("availableAgents", new String[]{"lineage_agent", "metadata_agent"});
        status.put("modes", new String[]{"react", "async"});
        status.put("status", "running");
        
        return ResponseEntity.ok(status);
    }

    /**
     * 健康检查接口
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "metaloom-ai-orchestrator");
        
        return ResponseEntity.ok(health);
    }

    /**
     * 分析请求模型
     */
    public static class AnalysisRequest {
        private String query;
        private String mode; // "react" 或 "async"

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }
} 