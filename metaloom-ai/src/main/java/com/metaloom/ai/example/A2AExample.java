package com.metaloom.ai.example;

import com.metaloom.ai.orchestrator.DataAnalysisOrchestrator;
import com.metaloom.ai.orchestrator.model.AnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * A2A智能体系统使用示例
 */
@Slf4j
@Component
public class A2AExample implements CommandLineRunner {

    @Autowired
    private DataAnalysisOrchestrator orchestrator;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== A2A智能体系统示例 ===");
        
        // 示例1：血缘关系查询
        demonstrateLineageQuery();
        
        // 示例2：元数据查询
        demonstrateMetadataQuery();
        
        // 示例3：综合分析
        demonstrateComprehensiveAnalysis();
        
        log.info("=== 示例执行完成 ===");
    }

    /**
     * 演示血缘关系查询
     */
    private void demonstrateLineageQuery() {
        log.info("--- 示例1：血缘关系查询 ---");
        
        String query = "查询用户表的数据血缘关系，包括上游和下游依赖";
        log.info("查询: {}", query);
        
        try {
            AnalysisResult result = orchestrator.processAnalysis(query);
            log.info("结果: {}", result.getFinalAnswer());
            log.info("处理时间: {}ms", result.getProcessingTime());
            log.info("步骤数: {}", result.getSteps().size());
        } catch (Exception e) {
            log.error("血缘关系查询失败", e);
        }
    }

    /**
     * 演示元数据查询
     */
    private void demonstrateMetadataQuery() {
        log.info("--- 示例2：元数据查询 ---");
        
        String query = "查询用户表的字段结构，包括字段类型和注释";
        log.info("查询: {}", query);
        
        try {
            AnalysisResult result = orchestrator.processAnalysis(query);
            log.info("结果: {}", result.getFinalAnswer());
            log.info("处理时间: {}ms", result.getProcessingTime());
            log.info("步骤数: {}", result.getSteps().size());
        } catch (Exception e) {
            log.error("元数据查询失败", e);
        }
    }

    /**
     * 演示综合分析
     */
    private void demonstrateComprehensiveAnalysis() {
        log.info("--- 示例3：综合分析 ---");
        
        String query = "分析用户表的完整信息，包括结构、血缘关系和数据质量";
        log.info("查询: {}", query);
        
        try {
            AnalysisResult result = orchestrator.processAnalysisAsync(query);
            log.info("结果: {}", result.getFinalAnswer());
            log.info("处理时间: {}ms", result.getProcessingTime());
        } catch (Exception e) {
            log.error("综合分析失败", e);
        }
    }
} 