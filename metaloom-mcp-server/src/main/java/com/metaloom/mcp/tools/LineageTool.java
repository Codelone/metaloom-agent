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

    // 本地调试模式开关
    private static final boolean LOCAL_DEBUG_MODE = true;

    @Autowired(required = false)
    private TMtdMdLineageService lineageService;

    /**
     * 根据元数据ID查询上游血缘关系
     * 查询指定元数据的所有上游数据来源
     */
    @Tool(name = "queryUpstreamLineage", description = "查询元数据的上游血缘关系（数据来源）")
    public String queryUpstreamLineage(@ToolParam(description = "元数据ID") String instId) {
        if (LOCAL_DEBUG_MODE) {
            return getMockUpstreamLineage(instId);
        }
        
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
        if (LOCAL_DEBUG_MODE) {
            return getMockDownstreamLineage(instId);
        }
        
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
        if (LOCAL_DEBUG_MODE) {
            return getMockFullLineage(instId);
        }
        
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

    /**
     * 获取模拟的上游血缘关系数据
     */
    private String getMockUpstreamLineage(String instId) {
        System.out.println("【本地调试模式】查询上游血缘关系，instId: " + instId);
        
        JSONArray resultArray = new JSONArray();
        
        // 根据图片中的血缘关系表数据创建模拟血缘关系
        switch (instId) {
            case "T001":
                // T001的上游：S001 (script, ETL)
                resultArray.add(createMockLineageItem("S001", "/etl/scripts/S001", "script", "ETL", "L001", "2024-07-01 10:00:00"));
                break;
            case "T002":
                // T002的上游：T001 (table, DWH)
                resultArray.add(createMockLineageItem("T001", "/dwh/ads_tv_fqz_order", "table", "DWH", "L002", "2024-07-02 02:30:00"));
                break;
            case "T003":
                // T003的上游：T002 (table, DWH)
                resultArray.add(createMockLineageItem("T002", "/dwh/ads_tv_company", "table", "DWH", "L003", "2024-07-03 15:45:00"));
                break;
            case "T004":
                // T004的上游：API1 (rest_api, CRM)
                resultArray.add(createMockLineageItem("API1", "/crm/api/user_info", "rest_api", "CRM", "L004", "2024-07-04 09:10:00"));
                break;
            case "T005":
                // T005的上游：T004 (table, DWH)
                resultArray.add(createMockLineageItem("T004", "/dwh/user_info", "table", "DWH", "L005", "2024-07-05 11:20:00"));
                break;
            case "T006":
                // T006的上游：FILE1 (file, FTP)
                resultArray.add(createMockLineageItem("FILE1", "/ftp/orders/order_data.csv", "file", "FTP", "L006", "2024-07-06 11:00:00"));
                break;
            case "T007":
                // T007的上游：T006 (table, DWH)
                resultArray.add(createMockLineageItem("T006", "/dwh/order_main", "table", "DWH", "L007", "2024-07-07 14:30:00"));
                break;
            case "T008":
                // T008的上游：T007 (table, DWH)
                resultArray.add(createMockLineageItem("T007", "/dwh/order_detail", "table", "DWH", "L008", "2024-07-08 16:45:00"));
                break;
            case "T009":
                // T009的上游：T005 (table, DWH)
                resultArray.add(createMockLineageItem("T005", "/dwh/user_profile", "table", "DWH", "L009", "2024-07-09 12:00:00"));
                break;
            case "T010":
                // T010的上游：T009 (view, DWH)
                resultArray.add(createMockLineageItem("T009", "/dwh/sales_summary", "view", "DWH", "L010", "2024-07-10 08:15:00"));
                break;
            default:
                // 默认返回空结果
                break;
        }
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("count", resultArray.size());
        result.put("data", resultArray);
        
        System.out.println("【本地调试模式】返回上游血缘关系: " + result.toJSONString());
        return result.toJSONString();
    }

    /**
     * 获取模拟的下游血缘关系数据
     */
    private String getMockDownstreamLineage(String instId) {
        System.out.println("【本地调试模式】查询下游血缘关系，instId: " + instId);
        
        JSONArray resultArray = new JSONArray();
        
        // 根据图片中的血缘关系表数据创建模拟血缘关系
        switch (instId) {
            case "S001":
                // S001的下游：T001
                resultArray.add(createMockLineageItem("T001", "/dwh/ads_tv_fqz_order", "table", "DWH", "L001", "2024-07-01 10:00:00"));
                break;
            case "T001":
                // T001的下游：T002
                resultArray.add(createMockLineageItem("T002", "/dwh/ads_tv_company", "table", "DWH", "L002", "2024-07-02 02:30:00"));
                break;
            case "T002":
                // T002的下游：T003
                resultArray.add(createMockLineageItem("T003", "/dwh/ads_tv_company_collect", "table", "DWH", "L003", "2024-07-03 15:45:00"));
                break;
            case "API1":
                // API1的下游：T004
                resultArray.add(createMockLineageItem("T004", "/dwh/user_info", "table", "DWH", "L004", "2024-07-04 09:10:00"));
                break;
            case "T004":
                // T004的下游：T005
                resultArray.add(createMockLineageItem("T005", "/dwh/user_profile", "table", "DWH", "L005", "2024-07-05 11:20:00"));
                break;
            case "FILE1":
                // FILE1的下游：T006
                resultArray.add(createMockLineageItem("T006", "/dwh/order_main", "table", "DWH", "L006", "2024-07-06 11:00:00"));
                break;
            case "T006":
                // T006的下游：T007
                resultArray.add(createMockLineageItem("T007", "/dwh/order_detail", "table", "DWH", "L007", "2024-07-07 14:30:00"));
                break;
            case "T007":
                // T007的下游：T008
                resultArray.add(createMockLineageItem("T008", "/dwh/product_info", "table", "DWH", "L008", "2024-07-08 16:45:00"));
                break;
            case "T005":
                // T005的下游：T009
                resultArray.add(createMockLineageItem("T009", "/dwh/sales_summary", "view", "DWH", "L009", "2024-07-09 12:00:00"));
                break;
            case "T009":
                // T009的下游：T010
                resultArray.add(createMockLineageItem("T010", "/dwh/customer_analysis", "table", "DWH", "L010", "2024-07-10 08:15:00"));
                break;
            default:
                // 默认返回空结果
                break;
        }
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("count", resultArray.size());
        result.put("data", resultArray);
        
        System.out.println("【本地调试模式】返回下游血缘关系: " + result.toJSONString());
        return result.toJSONString();
    }

    /**
     * 获取模拟的完整血缘关系数据
     */
    private String getMockFullLineage(String instId) {
        System.out.println("【本地调试模式】查询完整血缘关系，instId: " + instId);
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("nodeId", instId);
        
        // 获取上游关系
        String upstreamResponse = getMockUpstreamLineage(instId);
        JSONObject upstreamJson = JSON.parseObject(upstreamResponse);
        result.put("upstream", upstreamJson.getJSONArray("data"));
        result.put("upstreamCount", upstreamJson.getIntValue("count"));
        
        // 获取下游关系
        String downstreamResponse = getMockDownstreamLineage(instId);
        JSONObject downstreamJson = JSON.parseObject(downstreamResponse);
        result.put("downstream", downstreamJson.getJSONArray("data"));
        result.put("downstreamCount", downstreamJson.getIntValue("count"));
        
        System.out.println("【本地调试模式】返回完整血缘关系: " + result.toJSONString());
        return result.toJSONString();
    }

    /**
     * 创建模拟的血缘关系项
     */
    private JSONObject createMockLineageItem(String id, String path, String classId, String sysId, String relationId, String relationTime) {
        JSONObject item = new JSONObject();
        item.put("sourceId", id);
        item.put("sourcePath", path);
        item.put("sourceClassId", classId);
        item.put("sourceSysId", sysId);
        item.put("relationId", relationId);
        item.put("relationTime", relationTime);
        return item;
    }
} 
