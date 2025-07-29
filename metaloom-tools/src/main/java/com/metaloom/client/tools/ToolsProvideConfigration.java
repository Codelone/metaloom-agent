package com.metaloom.client.tools;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ToolsProvideConfigration {
    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @PostConstruct
    public ToolCallbackProvider getToolCallbackProvider() {
        return toolCallbackProvider;
    }
}
