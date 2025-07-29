package com.metaloom.mcp;

import com.metaloom.mcp.tools.MetadataTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.metaloom"})
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider imageSearchTools(MetadataTool metadataTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(metadataTool)
                .build();
    }
}