package com.metaloom.mcp.tools;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import com.metaloom.common.http.HttpClientUtils;
import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;


@Service
public class MetadataTool {

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
    public String metadataDetailTool(@ToolParam(description = "元数据instId") String instId) {
        String url = "http://10.4.96.232:8002/api/mtd/employ/query/mdInst/queryAttrInfoList";
        Map<String, Object> body = new HashMap<>();
        body.put("classId", "OdpsTable");
        body.put("instId", instId);
        body.put("envCode", "prod");
        String resp = HttpClientUtils.postJson(url, body);
        JSONObject json = JSON.parseObject(resp);
        JSONObject data = json.getJSONObject("data");
        JSONObject valueMap = data != null ? data.getJSONObject("valueMap") : null;
        JSONObject instMap = data != null ? data.getJSONObject("instMap") : null;
        JSONObject ret = new JSONObject();
        if (valueMap != null) {
            ret.put("valueMap", valueMap);
        }
        if (instMap != null) {
            ret.put("instMap", instMap);
        }
        return ret.toJSONString();
    }
}