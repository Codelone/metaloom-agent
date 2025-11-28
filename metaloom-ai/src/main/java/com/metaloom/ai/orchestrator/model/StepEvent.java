package com.metaloom.ai.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepEvent {

    private int iteration;

    private String type; // step_start, agent_result, final_answer, error

    private String agentName;

    private String request;

    private String result;

    private String finalAnswer;

    private Instant timestamp;

    private Map<String, Object> metadata;
} 