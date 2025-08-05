package com.metaloom.ai.orchestrator.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体行动
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentAction {
    
    /**
     * 行动类型
     */
    private ActionType type;
    
    /**
     * 智能体名称
     */
    private String agentName;
    
    /**
     * 请求体
     */
    private String requestBody;
    
    /**
     * 推理过程
     */
    private String reasoning;
    
    /**
     * 并行任务列表
     */
    @Builder.Default
    private List<AgentTask> parallelTasks = new ArrayList<>();
    
    /**
     * 是否包含并行任务
     */
    public boolean hasParallelTasks() {
        return parallelTasks != null && !parallelTasks.isEmpty();
    }
} 