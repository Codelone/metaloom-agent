package com.metaloom.ai.orchestrator.model;

/**
 * 行动类型枚举
 */
public enum ActionType {
    
    /**
     * 调用智能体
     */
    CALL_AGENT,
    
    /**
     * 并行执行多个任务
     */
    PARALLEL_TASKS,
    
    /**
     * 最终答案
     */
    FINAL_ANSWER
} 