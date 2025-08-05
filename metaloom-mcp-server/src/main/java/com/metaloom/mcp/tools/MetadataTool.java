package com.metaloom.mcp.tools;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import com.metaloom.common.http.HttpClientUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;


@Service
public class MetadataTool {

    // 本地调试模式开关
    private static final boolean LOCAL_DEBUG_MODE = true;

    /**
     * 元数据列表
     * api：http://10.4.96.232:8002/api/mtd/employ/query/mdInstEs/list
     * 请求体：{"pageSize":10,"pageNum":1,"orderName":"","orderValue":"","envCode":"prod","flag":"false","instName":"ads_tv","instCode":"","sysId":"","schemaCode":"","classId":""}
     * 响应体{"total":6624691,"pageSize":10,"pageNum":1,"pages":0,"data":[{"id":"6a19d1b55c0a1b9572a567b8faebf8dc_prod","instId":"6a19d1b55c0a1b9572a567b8faebf8dc","parentId":"0a1cbc65b7e195b76ee9b49d1fa5d5a1","instCode":"<em>ads_tv</em>_fqz_order","instName":"<em>ads_tv</em>_fqz_order","schema":"lab_sharedata_dev","instCodePath":"/lab_sharedata_dev/ads_tv_fqz_order","versionNo":5,"sysId":"SY0p7QSG749lS","startTime":"2025-07-21 07:59:42","classId":"OdpsTable","className":"Odps表","envCode":"prod","viewNum":3,"count":2,"sysName":"数据实验室"},{"id":"c194bffa377fa699f9369ec028466174_prod","instId":"c194bffa377fa699f9369ec028466174","parentId":"c91919dc11ce1ebe11d5a52932dd2044","instCode":"<em>ads_tv</em>_company","instName":"对公客户实体表：<em>ADS_TV</em>_COMPANY","schema":"app_tlkg","instCodePath":"/app_tlkg/ads_tv_company","versionNo":43,"sysId":"SY0p7QUt1oPmM","startTime":"2025-07-24 07:51:20","classId":"OdpsTable","className":"Odps表","envCode":"prod","viewNum":3,"count":75,"sysName":"知识图谱平台"},{"id":"225974a596a900ca40a9719c8d694530_prod","instId":"225974a596a900ca40a9719c8d694530","parentId":"c91919dc11ce1ebe11d5a52932dd2044","instCode":"<em>ads_tv</em>_company_collect","instName":"对公客户实体详情表：<em>ADS_TV</em>_COMPANY_COLLECT","schema":"app_tlkg","instCodePath":"/app_tlkg/ads_tv_company_collect","versionNo":18,"sysId":"SY0p7QUt1oPmM","startTime":"2025-07-23 07:50:39","classId":"OdpsTable","className":"Odps表","envCode":"prod","viewNum":0,"count":72,"sysName":"知识图谱平台"},{"id":"6fa831bb261f9da28931c42efe7146cf_prod","instId":"6fa831bb261f9da28931c42efe7146cf","parentId":"c91919dc11ce1ebe11d5a52932dd2044","instCode":"<em>ads_tv</em>_unkown_cst_bak_20241024_472fe46d9f4147871e208835abb84e3f","instName":"<em>ads_tv</em>_unkown_cst_bak_20241024_472fe46d9f4147871e208835abb84e3f","schema":"app_tlkg","instCodePath":"/app_tlkg/ads_tv_unkown_cst_bak_20241024_472fe46d9f4147871e208835abb84e3f","versionNo":2,"sysId":"SY0p7QUt1oPmM","startTime":"2025-05-26 08:06:59","classId":"OdpsTable","className":"Odps表","envCode":"prod","viewNum":0,"count":3,"sysName":"知识图谱平台"},{"id":"e98169abbe4b7622669601e075e44fd2_prod","instId":"e98169abbe4b7622669601e075e44fd2","parentId":"c91919dc11ce1ebe11d5a52932dd2044","instCode":"721405","instName":"<em>ADS_TV</em>_FQZ_CUSTOMER","instCodePath":"/app_tlkg/721405","versionNo":1,"sysId":"SY0p7QUt1oPmM","startTime":"2025-06-12 14:42:50","classId":"OdpsPROC","className":"ODPS存储过程","envCode":"prod","viewNum":0,"count":0,"sysName":"知识图谱平台"},{"id":"a25dd924fc05a28a95791c2fdb263c39_prod","instId":"a25dd924fc05a28a95791c2fdb263c39","parentId":"c91919dc11ce1ebe11d5a52932dd2044","instCode":"721406","instName":"<em>ADS_TV</em>_FQZ_REFERENCE","instCodePath":"/app_tlkg/721406","versionNo":1,"sysId":"SY0p7QUt1oPmM","startTime":"2025-06-12 14:42:50","classId":"OdpsPROC","className":"ODPS存储过程","envCode":"prod","viewNum":0,"count":0,"sysName":"知识图谱平台"},{"id":"1a56fa5bd162a279f89531c0796d5f2d_prod","instId":"1a56fa5bd162a279f89531c0796d5f2d","parentId":"c91919dc11ce1ebe11d5a52932dd2044","instCode":"721412","instName":"<em>ADS_TV</em>_FQZ_PHONE","instCodePath":"/app_tlkg/721412","versionNo":1,"sysId":"SY0p7QUt1oPmM","startTime":"2025-06-12 14:42:50","classId":"OdpsPROC","className":"ODPS存储过程","envCode":"prod","viewNum":0,"count":0,"sysName":"知识图谱平台"},{"id":"b02c7ec95175d3435ec48f085a5f5eb3_prod","instId":"b02c7ec95175d3435ec48f085a5f5eb3","parentId":"c91919dc11ce1ebe11d5a52932dd2044","instCode":"721414","instName":"<em>ADS_TV</em>_FQZ_WIFI","instCodePath":"/app_tlkg/721414","versionNo":1,"sysId":"SY0p7QUt1oPmM","startTime":"2025-06-12 14:42:50","classId":"OdpsPROC","className":"ODPS存储过程","envCode":"prod","viewNum":0,"count":0,"sysName":"知识图谱平台"},{"id":"74fc7f3ada2bb0aaed837d1d43754f96_prod","instId":"74fc7f3ada2bb0aaed837d1d43754f96","parentId":"c91919dc11ce1ebe11d5a52932dd2044","instCode":"721416","instName":"<em>ADS_TV</em>_FQZ_DEVICE","instCodePath":"/app_tlkg/721416","versionNo":1,"sysId":"SY0p7QUt1oPmM","startTime":"2025-06-12 14:42:50","classId":"OdpsPROC","className":"ODPS存储过程","envCode":"prod","viewNum":0,"count":0,"sysName":"知识图谱平台"},{"id":"3c92434c4cce850e48db761b25e8c552_prod","instId":"3c92434c4cce850e48db761b25e8c552","parentId":"c91919dc11ce1ebe11d5a52932dd2044","instCode":"721418","instName":"<em>ADS_TV</em>_FQZ_WORK","instCodePath":"/app_tlkg/721418","versionNo":1,"sysId":"SY0p7QUt1oPmM","startTime":"2025-06-12 14:42:50","classId":"OdpsPROC","className":"ODPS存储过程","envCode":"prod","viewNum":0,"count":0,"sysName":"知识图谱平台"}]}
     *
     * 元数据详情
     * api：http://10.4.96.232:8002/api/mtd/employ/query/mdInst/queryAttrInfoList
     * 请求体：{"classId":"OdpsTable","instId":"6a19d1b55c0a1b9572a567b8faebf8dc","envCode":"prod"}
     * 响应体：{"code":"0000","msg":"操作成功","data":{"valueMap":{"源系统状态":"已上线","90天访问次数":"3","近30天访问次数":"0","存储空间":"0.00GB","创建时间":"2025-06-03 17:13:25","表DDL更新时间":"2025-06-03 17:13:25","数据更新方式":"未知","表数据更新时间":"2025-06-03 17:13:25","多法人机构号":"999999998","表OWNER的KP":"p4_255015627289330905","表OWNER的云账号名称":"RAM$dtdep-22-1553494350055:024693_SZCQQFUF","表的最后访问时间":"0","数据库名称":"lab_sharedata_dev","数据库类型":"odps","数据类型":"TABLE","记录数":"152,985条","存储量":"0.00GB","更新频率":"未知"},"valueBusiMap":{},"instMap":{"元数据类型":"Odps表","系统名称":"数据实验室","元数据路径":"/lab_sharedata_dev/ads_tv_fqz_order","元数据中文名":"ads_tv_fqz_order","元数据英文名":"ads_tv_fqz_order"}},"success":true}
     */

    @Tool(name = "metadataListTool", description = "查询元数据列表")
    public String metadataListTool(@ToolParam(description = "查询元数据关键字") String keyword) {
        if (LOCAL_DEBUG_MODE) {
            return getMockMetadataList(keyword);
        }
        
        String url = "http://10.4.96.232:8002/api/mtd/employ/query/mdInstEs/list";
        Map<String, Object> body = new HashMap<>();
        body.put("pageSize", 10);
        body.put("pageNum", 1);
        body.put("orderName", "");
        body.put("orderValue", "");
        body.put("envCode", "prod");
        body.put("flag", "false");
        body.put("instName", keyword);
        body.put("instCode", "");
        body.put("sysId", "");
        body.put("schemaCode", "");
        body.put("classId", "");
        String resp = HttpClientUtils.postJson(url, body);
        JSONObject json = JSON.parseObject(resp);
        JSONArray data = json.getJSONArray("data");
        JSONArray result = new JSONArray();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                JSONObject item = data.getJSONObject(i);
                JSONObject obj = new JSONObject();
                obj.put("id", item.getString("id"));
                obj.put("instId", item.getString("instId"));
                obj.put("instName", item.getString("instName"));
                obj.put("className", item.getString("className"));
                obj.put("envCode", item.getString("envCode"));
                obj.put("sysName", item.getString("sysName"));
                obj.put("schema", item.getString("schema"));
                obj.put("count", item.getString("count"));
                result.add(obj);
            }
        }
        JSONObject ret = new JSONObject();
        ret.put("total", json.getIntValue("total"));
        ret.put("list", result);
        return ret.toJSONString();
    }

    @Tool(name = "metadataDetailTool", description = "查询元数据详情")
    public List<String> metadataDetailTool(@ToolParam(description = "元数据instId列表") List<String> instIds) {
        if (LOCAL_DEBUG_MODE) {
            return getMockMetadataDetails(instIds);
        }
        
        List<String> results = new ArrayList<>();
        
        for (String instId : instIds) {
            String url = "http://10.4.96.232:8002/api/mtd/employ/query/mdInst/queryAttrInfoList";
            Map<String, Object> body = new HashMap<>();
            body.put("classId", "OdpsTable");
            body.put("instId", instId);
            body.put("envCode", "prod");
            
            try {
                String resp = HttpClientUtils.postJson(url, body);
                JSONObject json = JSON.parseObject(resp);
                JSONObject data = json.getJSONObject("data");
                JSONObject valueMap = data != null ? data.getJSONObject("valueMap") : null;
                JSONObject instMap = data != null ? data.getJSONObject("instMap") : null;
                
                JSONObject ret = new JSONObject();
                ret.put("instId", instId);
                if (valueMap != null) {
                    ret.put("valueMap", valueMap);
                }
                if (instMap != null) {
                    ret.put("instMap", instMap);
                }
                results.add(ret.toJSONString());
            } catch (Exception e) {
                // 如果单个请求失败，添加错误信息到结果中
                JSONObject errorResult = new JSONObject();
                errorResult.put("instId", instId);
                errorResult.put("error", "查询失败: " + e.getMessage());
                results.add(errorResult.toJSONString());
            }
        }
        
        return results;
    }

    /**
     * 获取模拟的元数据列表数据
     */
    private String getMockMetadataList(String keyword) {
        System.out.println("【本地调试模式】查询关键字: " + keyword);
        
        JSONArray mockData = new JSONArray();
        
        // 根据关键字返回不同的模拟数据
        if (keyword != null && keyword.toLowerCase().contains("ads_tv")) {
            // ads_tv相关的模拟数据
            mockData.add(createMockMetadataItem("T001_prod", "T001", 
                "ads_tv_fqz_order", "ads_tv_fqz_order", "Odps表", "prod", "数据实验室", "lab_sharedata_dev", "152,985"));
            mockData.add(createMockMetadataItem("T002_prod", "T002", 
                "ads_tv_company", "对公客户实体表：ADS_TV_COMPANY", "Odps表", "prod", "知识图谱平台", "app_tlkg", "75"));
            mockData.add(createMockMetadataItem("T003_prod", "T003", 
                "ads_tv_company_collect", "对公客户实体详情表：ADS_TV_COMPANY_COLLECT", "Odps表", "prod", "知识图谱平台", "app_tlkg", "72"));
        } else if (keyword != null && keyword.toLowerCase().contains("user")) {
            // user相关的模拟数据
            mockData.add(createMockMetadataItem("T004_prod", "T004", 
                "user_info", "用户信息表", "Odps表", "prod", "用户中心", "user_center", "1,234,567"));
            mockData.add(createMockMetadataItem("T005_prod", "T005", 
                "user_profile", "用户档案表", "Odps表", "prod", "用户中心", "user_center", "890,123"));
        } else if (keyword != null && keyword.toLowerCase().contains("order")) {
            // order相关的模拟数据
            mockData.add(createMockMetadataItem("T006_prod", "T006", 
                "order_main", "订单主表", "Odps表", "prod", "订单系统", "order_system", "5,678,901"));
            mockData.add(createMockMetadataItem("T007_prod", "T007", 
                "order_detail", "订单详情表", "Odps表", "prod", "订单系统", "order_system", "12,345,678"));
        } else if (keyword != null && keyword.toLowerCase().contains("t001") || keyword.toLowerCase().contains("t002") || keyword.toLowerCase().contains("t003")) {
            // T001-T003相关的模拟数据
            mockData.add(createMockMetadataItem("T001_prod", "T001", 
                "ads_tv_fqz_order", "ads_tv_fqz_order", "Odps表", "prod", "数据实验室", "lab_sharedata_dev", "152,985"));
            mockData.add(createMockMetadataItem("T002_prod", "T002", 
                "ads_tv_company", "对公客户实体表：ADS_TV_COMPANY", "Odps表", "prod", "知识图谱平台", "app_tlkg", "75"));
            mockData.add(createMockMetadataItem("T003_prod", "T003", 
                "ads_tv_company_collect", "对公客户实体详情表：ADS_TV_COMPANY_COLLECT", "Odps表", "prod", "知识图谱平台", "app_tlkg", "72"));
        } else if (keyword != null && keyword.toLowerCase().contains("t004") || keyword.toLowerCase().contains("t005") || keyword.toLowerCase().contains("t006")) {
            // T004-T006相关的模拟数据
            mockData.add(createMockMetadataItem("T004_prod", "T004", 
                "user_info", "用户信息表", "Odps表", "prod", "用户中心", "user_center", "1,234,567"));
            mockData.add(createMockMetadataItem("T005_prod", "T005", 
                "user_profile", "用户档案表", "Odps表", "prod", "用户中心", "user_center", "890,123"));
            mockData.add(createMockMetadataItem("T006_prod", "T006", 
                "order_main", "订单主表", "Odps表", "prod", "订单系统", "order_system", "5,678,901"));
        } else if (keyword != null && keyword.toLowerCase().contains("t007") || keyword.toLowerCase().contains("t008") || keyword.toLowerCase().contains("t009") || keyword.toLowerCase().contains("t010")) {
            // T007-T010相关的模拟数据
            mockData.add(createMockMetadataItem("T007_prod", "T007", 
                "order_detail", "订单详情表", "Odps表", "prod", "订单系统", "order_system", "12,345,678"));
            mockData.add(createMockMetadataItem("T008_prod", "T008", 
                "product_info", "产品信息表", "Odps表", "prod", "产品系统", "product_system", "45,678"));
            mockData.add(createMockMetadataItem("T009_prod", "T009", 
                "sales_summary", "销售汇总视图", "Odps视图", "prod", "销售系统", "sales_system", "89,012"));
            mockData.add(createMockMetadataItem("T010_prod", "T010", 
                "customer_analysis", "客户分析表", "Odps表", "prod", "分析系统", "analysis_system", "234,567"));
        } else {
            // 默认模拟数据 - 返回所有可用的表
            mockData.add(createMockMetadataItem("T001_prod", "T001", 
                "ads_tv_fqz_order", "ads_tv_fqz_order", "Odps表", "prod", "数据实验室", "lab_sharedata_dev", "152,985"));
            mockData.add(createMockMetadataItem("T002_prod", "T002", 
                "ads_tv_company", "对公客户实体表：ADS_TV_COMPANY", "Odps表", "prod", "知识图谱平台", "app_tlkg", "75"));
            mockData.add(createMockMetadataItem("T003_prod", "T003", 
                "ads_tv_company_collect", "对公客户实体详情表：ADS_TV_COMPANY_COLLECT", "Odps表", "prod", "知识图谱平台", "app_tlkg", "72"));
            mockData.add(createMockMetadataItem("T004_prod", "T004", 
                "user_info", "用户信息表", "Odps表", "prod", "用户中心", "user_center", "1,234,567"));
            mockData.add(createMockMetadataItem("T005_prod", "T005", 
                "user_profile", "用户档案表", "Odps表", "prod", "用户中心", "user_center", "890,123"));
            mockData.add(createMockMetadataItem("T006_prod", "T006", 
                "order_main", "订单主表", "Odps表", "prod", "订单系统", "order_system", "5,678,901"));
            mockData.add(createMockMetadataItem("T007_prod", "T007", 
                "order_detail", "订单详情表", "Odps表", "prod", "订单系统", "order_system", "12,345,678"));
            mockData.add(createMockMetadataItem("T008_prod", "T008", 
                "product_info", "产品信息表", "Odps表", "prod", "产品系统", "product_system", "45,678"));
            mockData.add(createMockMetadataItem("T009_prod", "T009", 
                "sales_summary", "销售汇总视图", "Odps视图", "prod", "销售系统", "sales_system", "89,012"));
            mockData.add(createMockMetadataItem("T010_prod", "T010", 
                "customer_analysis", "客户分析表", "Odps表", "prod", "分析系统", "analysis_system", "234,567"));
        }
        
        JSONObject result = new JSONObject();
        result.put("total", mockData.size());
        result.put("list", mockData);
        
        System.out.println("【本地调试模式】返回模拟数据: " + result.toJSONString());
        return result.toJSONString();
    }

    /**
     * 创建模拟的元数据项
     */
    private JSONObject createMockMetadataItem(String id, String instId, String instCode, String instName, 
                                            String className, String envCode, String sysName, String schema, String count) {
        JSONObject item = new JSONObject();
        item.put("id", id);
        item.put("instId", instId);
        item.put("instName", instName);
        item.put("className", className);
        item.put("envCode", envCode);
        item.put("sysName", sysName);
        item.put("schema", schema);
        item.put("count", count);
        return item;
    }

    /**
     * 获取模拟的元数据详情数据
     */
    private List<String> getMockMetadataDetails(List<String> instIds) {
        System.out.println("【本地调试模式】查询instIds: " + instIds);
        
        List<String> results = new ArrayList<>();
        
        for (String instId : instIds) {
            JSONObject mockDetail = new JSONObject();
            mockDetail.put("instId", instId);
            
            // 创建模拟的valueMap
            JSONObject valueMap = new JSONObject();
            valueMap.put("源系统状态", "已上线");
            valueMap.put("90天访问次数", "15");
            valueMap.put("近30天访问次数", "3");
            valueMap.put("存储空间", "2.5GB");
            valueMap.put("创建时间", "2024-01-15 10:30:00");
            valueMap.put("表DDL更新时间", "2024-12-01 14:20:00");
            valueMap.put("数据更新方式", "增量更新");
            valueMap.put("表数据更新时间", "2024-12-15 08:00:00");
            valueMap.put("数据库名称", "prod_database");
            valueMap.put("数据库类型", "odps");
            valueMap.put("数据类型", "TABLE");
            valueMap.put("记录数", "1,234,567条");
            valueMap.put("存储量", "2.5GB");
            valueMap.put("更新频率", "每日");
            
            // 创建模拟的instMap
            JSONObject instMap = new JSONObject();
            instMap.put("元数据类型", "Odps表");
            instMap.put("系统名称", "模拟系统");
            instMap.put("元数据路径", "/prod_database/" + instId);
            instMap.put("元数据中文名", "模拟表_" + instId);
            instMap.put("元数据英文名", instId);
            
            mockDetail.put("valueMap", valueMap);
            mockDetail.put("instMap", instMap);
            
            results.add(mockDetail.toJSONString());
        }
        
        System.out.println("【本地调试模式】返回模拟详情数据: " + results);
        return results;
    }
}
