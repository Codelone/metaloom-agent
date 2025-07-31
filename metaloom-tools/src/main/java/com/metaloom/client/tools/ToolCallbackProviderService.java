package com.metaloom.client.tools;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.function.Predicate;

@Service
public class ToolCallbackProviderService {
    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @PostConstruct
    public void init() {
        // 检查tools
        for (ToolCallback toolCallback : toolCallbackProvider.getToolCallbacks()) {
            System.out.println(toolCallback.getToolDefinition().name());
        }
    }

    /**
     * 获取所有工具的ToolCallbackProvider
     */
    public ToolCallbackProvider getToolCallbackProvider() {
        return toolCallbackProvider;
    }

    /**
     * 获取血缘关系工具的ToolCallbackProvider
     * 只包含LineageTool中的工具
     */
    public ToolCallbackProvider getLineageToolCallbackProvider() {
        return createFilteredToolCallbackProvider(callback -> callback.getToolDefinition().name().toLowerCase().contains("lineage"));
    }

    /**
     * 获取元数据工具的ToolCallbackProvider
     * 只包含MetadataTool中的工具
     */
    public ToolCallbackProvider getMetadataToolCallbackProvider() {
        return createFilteredToolCallbackProvider(callback -> callback.getToolDefinition().name().toLowerCase().contains("metadata"));
    }

    /**
     * 创建过滤后的ToolCallbackProvider
     *
     * @param filter 工具回调过滤条件
     * @return 过滤后的ToolCallbackProvider
     */
    private ToolCallbackProvider createFilteredToolCallbackProvider(Predicate<ToolCallback> filter) {
        ToolCallback[] allCallbacks = toolCallbackProvider.getToolCallbacks();
        ToolCallback[] filteredCallbacks = Arrays.stream(allCallbacks)
                .filter(filter)
                .toArray(ToolCallback[]::new);

        return ToolCallbackProvider.from(filteredCallbacks);
    }
}
