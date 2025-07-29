package com.metaloom.mcp.tools;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;


@Service
public class MetadataTool {


    @Tool( description = "查询元数据列表")
    public String metadataListTool(@ToolParam(description = "查询元数据关键字") String keyword) {
        return "返回元数据查询的列表";
    }
}