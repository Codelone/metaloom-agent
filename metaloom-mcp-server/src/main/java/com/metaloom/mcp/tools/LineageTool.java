package com.metaloom.mcp.tools;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.metaloom.common.jdbc.entity.TMtdMdLineage;
import com.metaloom.common.jdbc.service.TMtdMdLineageService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 元数据血缘关系工具类
 */
@Service
public class LineageTool {

    @Autowired(required = false)
    private TMtdMdLineageService lineageService;

    /**
     * 根据元数据ID查询上游血缘关系
     * 查询指定元数据的所有上游数据来源
     */
    @Tool(name = "queryUpstreamLineage", description = "查询元数据的上游血缘关系（数据来源）")
    public String queryUpstreamLineage(@ToolParam(description = "元数据ID") String instId) {
        try {
            if (lineageService == null) {
                return createErrorResponse("血缘关系服务未注入，请检查配置");
            }
            
            // 查询当前元数据作为目标的所有血缘关系（即上游关系）
            List<TMtdMdLineage> upstreamList = lineageService.lambdaQuery()
                    .eq(TMtdMdLineage::getTargetInstId, instId)
                    .list();
            
            // 转换为前端友好的格式
            JSONArray resultArray = new JSONArray();
            for (TMtdMdLineage lineage : upstreamList) {
                JSONObject item = new JSONObject();
                item.put("sourceId", lineage.getSourceInstId());
                item.put("sourcePath", lineage.getSourceInstPath());
                item.put("sourceClassId", lineage.getSourceClassId());
                item.put("sourceSysId", lineage.getSourceSysId());
                item.put("relationId", lineage.getLineageId());
                item.put("relationTime", lineage.getStartTime());
                resultArray.add(item);
            }
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("count", upstreamList.size());
            result.put("data", resultArray);
            
            return result.toJSONString();
        } catch (Exception e) {
            return createErrorResponse("查询上游血缘关系失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据元数据ID查询下游血缘关系
     * 查询哪些元数据依赖于指定元数据
     */
    @Tool(name = "queryDownstreamLineage", description = "查询元数据的下游血缘关系（数据去向）")
    public String queryDownstreamLineage(@ToolParam(description = "元数据ID") String instId) {
        try {
            if (lineageService == null) {
                return createErrorResponse("血缘关系服务未注入，请检查配置");
            }
            
            // 查询当前元数据作为来源的所有血缘关系（即下游关系）
            List<TMtdMdLineage> downstreamList = lineageService.lambdaQuery()
                    .eq(TMtdMdLineage::getSourceInstId, instId)
                    .list();
            
            // 转换为前端友好的格式
            JSONArray resultArray = new JSONArray();
            for (TMtdMdLineage lineage : downstreamList) {
                JSONObject item = new JSONObject();
                item.put("targetId", lineage.getTargetInstId());
                item.put("targetPath", lineage.getTargetInstPath());
                item.put("targetClassId", lineage.getTargetClassId());
                item.put("targetSysId", lineage.getTargetSysId());
                item.put("relationId", lineage.getLineageId());
                item.put("relationTime", lineage.getStartTime());
                resultArray.add(item);
            }
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("count", downstreamList.size());
            result.put("data", resultArray);
            
            return result.toJSONString();
        } catch (Exception e) {
            return createErrorResponse("查询下游血缘关系失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询元数据的完整血缘关系图
     * 包含上下游所有关联
     */
    @Tool(name = "queryFullLineage", description = "查询元数据的完整血缘关系图")
    public String queryFullLineage(@ToolParam(description = "元数据ID") String instId) {
        try {
            if (lineageService == null) {
                return createErrorResponse("血缘关系服务未注入，请检查配置");
            }
            
            // 查询上游关系
            List<TMtdMdLineage> upstreamList = lineageService.lambdaQuery()
                    .eq(TMtdMdLineage::getTargetInstId, instId)
                    .list();
            
            // 查询下游关系
            List<TMtdMdLineage> downstreamList = lineageService.lambdaQuery()
                    .eq(TMtdMdLineage::getSourceInstId, instId)
                    .list();
            
            // 构建完整的血缘关系图
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("nodeId", instId);
            
            // 上游节点
            JSONArray upstreamArray = new JSONArray();
            for (TMtdMdLineage lineage : upstreamList) {
                JSONObject item = new JSONObject();
                item.put("sourceId", lineage.getSourceInstId());
                item.put("sourcePath", lineage.getSourceInstPath());
                item.put("relationId", lineage.getLineageId());
                upstreamArray.add(item);
            }
            result.put("upstream", upstreamArray);
            result.put("upstreamCount", upstreamList.size());
            
            // 下游节点
            JSONArray downstreamArray = new JSONArray();
            for (TMtdMdLineage lineage : downstreamList) {
                JSONObject item = new JSONObject();
                item.put("targetId", lineage.getTargetInstId());
                item.put("targetPath", lineage.getTargetInstPath());
                item.put("relationId", lineage.getLineageId());
                downstreamArray.add(item);
            }
            result.put("downstream", downstreamArray);
            result.put("downstreamCount", downstreamList.size());
            
            return result.toJSONString();
        } catch (Exception e) {
            return createErrorResponse("查询完整血缘关系失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建错误响应
     */
    private String createErrorResponse(String message) {
        JSONObject error = new JSONObject();
        error.put("success", false);
        error.put("message", message);
        return error.toJSONString();
    }
} 