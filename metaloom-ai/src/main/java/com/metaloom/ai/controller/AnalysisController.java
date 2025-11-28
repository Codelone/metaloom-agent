package com.metaloom.ai.controller;

import com.metaloom.ai.orchestrator.DataAnalysisOrchestrator;
import com.metaloom.ai.orchestrator.model.StepEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;
import java.time.Duration;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private DataAnalysisOrchestrator orchestrator;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody AnalysisRequest request, HttpServletResponse httpResponse) {
        // Reduce proxy/container buffering for SSE
        httpResponse.setHeader("Cache-Control", "no-cache");
        httpResponse.setHeader("X-Accel-Buffering", "no");
        httpResponse.setCharacterEncoding("UTF-8");
        SseEmitter emitter = new SseEmitter(0L);

        // Mock data for connectivity/style testing
        if (request != null && request.getQuery() != null && "mock".equalsIgnoreCase(request.getQuery())) {
            new Thread(() -> {
                try {
                    // Group 1: 数据解析
                    emitter.send(SseEmitter.event().name("数据解析").data(StepEvent.builder()
                        .type("chunk")
                        .result("正在解析问题...\n")
                        .build()));
                    Thread.sleep(400);
                    emitter.send(SseEmitter.event().name("数据解析").data(StepEvent.builder()
                        .type("chunk")
                        .result("提取关键实体：股票、时间范围\n")
                        .build()));
                    Thread.sleep(400);
                    emitter.send(SseEmitter.event().name("数据解析").data(StepEvent.builder()
                        .type("group_done")
                        .result("")
                        .build()));

                    // Group 2: 数据检索
                    emitter.send(SseEmitter.event().name("数据检索").data(StepEvent.builder()
                        .type("chunk")
                        .result("检索数据源：\n 行情与财报...\n")
                        .build()));
                    Thread.sleep(500);
                    emitter.send(SseEmitter.event().name("数据检索").data(StepEvent.builder()
                        .type("chunk")
                        .result("命中3条相关记录\n")
                        .build()));
                    Thread.sleep(500);
                    emitter.send(SseEmitter.event().name("数据检索").data(StepEvent.builder()
                        .type("group_done")
                        .result("")
                        .build()));

                    // Group 3: 分析结论
                    emitter.send(SseEmitter.event().name("分析结论").data(StepEvent.builder()
                        .type("chunk")
                        .result("综合走势与基本面...\n")
                        .build()));
                    Thread.sleep(500);
                    emitter.send(SseEmitter.event().name("分析结论").data(StepEvent.builder()
                        .type("chunk")
                        .result("AAPL 与 MSFT 具备中期上涨动能\n")
                        .build()));
                    Thread.sleep(400);
                    emitter.send(SseEmitter.event().name("分析结论").data(StepEvent.builder()
                        .type("group_done")
                        .result("")
                        .build()));

                    // Final answer
                    emitter.send(SseEmitter.event().name("final_answer").data(StepEvent.builder()
                        .type("final_answer")
                        .result("建议关注 AAPL 与 MSFT，短期回撤后分批布局，注意财报与宏观数据节奏。")
                        .build()));
                } catch (IOException | InterruptedException e) {
                    log.error("SSE 发送失败", e);
                    emitter.completeWithError(e);
                    return;
                }
                emitter.complete();
            }).start();
            return emitter;
        }

        Disposable subscription = orchestrator.processAnalysisStream(request.getQuery())
            .timeout(Duration.ofMinutes(10))
            .subscribe(event -> {
                try {
                    final String eventName;
                    if ("final_answer".equalsIgnoreCase(event.getType())) {
                        eventName = "final_answer";
                    } else if (event.getRequest() != null && !event.getRequest().isEmpty()) {
                        eventName = event.getRequest();
                    } else if (event.getType() != null) {
                        eventName = event.getType();
                    } else {
                        eventName = "message";
                    }
                    emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(event));
                } catch (IOException e) {
                    log.error("SSE 发送失败", e);
                    emitter.completeWithError(e);
                }
            }, error -> {
                try {
                    StepEvent err = StepEvent.builder()
                        .type("error")
                        .result(error.getMessage())
                        .build();
                    emitter.send(SseEmitter.event().name("error").data(err));
                } catch (IOException ignored) {}
                emitter.completeWithError(error);
            }, emitter::complete);

        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(() -> {
            subscription.dispose();
            emitter.complete();
        });
        return emitter;
    }

    @Data
    public static class AnalysisRequest {
        private String query;
    }
} 