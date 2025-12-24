package com.metaloom.ai.agent.impl;

import com.metaloom.ai.agent.AgentType;
import com.metaloom.ai.agent.ChatAgent;
import com.metaloom.ai.agent.nl2sql.service.TableMetadataService;
import com.metaloom.ai.agent.nl2sql.tool.NL2SQLToolsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * NL2SQL智能体
 * 将自然语言查询转换为SQL
 */
@Slf4j
@Component
public class NL2SQLAgent implements ChatAgent {

    private final TableMetadataService tableMetadataService;
    private final NL2SQLToolsService nl2sqlToolsService;
    private String systemPromptTemplate;

    @Autowired
    public NL2SQLAgent(TableMetadataService tableMetadataService,
            NL2SQLToolsService nl2sqlToolsService) {
        this.tableMetadataService = tableMetadataService;
        this.nl2sqlToolsService = nl2sqlToolsService;
    }

    @Override
    public void initialize() {
        log.info("初始化NL2SQL Agent...");
        buildSystemPromptTemplate();
        log.info("NL2SQL Agent初始化完成");
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.NL2SQL;
    }

    @Override
    public String getSystemPrompt(String sessionId) {
        // 系统提示词包含所有表的概要信息
        return systemPromptTemplate;
    }

    @Override
    public String process(String sessionId, String userPrompt, ChatClient chatClient) {
        log.debug("NL2SQL Agent processing: sessionId={}", sessionId);

        // 使用.tools()注册基于@Tool注解的工具服务实例
        return chatClient.prompt()
                .system(getSystemPrompt(sessionId))
                .user(userPrompt)
                .tools(nl2sqlToolsService)
                .call()
                .content();
    }

    @Override
    public Flux<String> processStream(String sessionId, String userPrompt, ChatClient chatClient) {
        log.debug("NL2SQL Agent streaming: sessionId={}", sessionId);

        return chatClient.prompt()
                .system(getSystemPrompt(sessionId))
                .user(userPrompt)
                .tools(nl2sqlToolsService)
                .stream()
                .content();
    }

    /**
     * 构建系统提示词模板
     */
    private void buildSystemPromptTemplate() {
        String tablesSummary = tableMetadataService.getTablesSummary();

        systemPromptTemplate = String.format("""
                你是一个专业的NL2SQL专家，负责将用户的自然语言查询转换为SQL语句。

                ## 数据库环境
                - 数据库类型: ODPS (MaxCompute)
                - 字符集: UTF-8

                ## 可用的工具

                1. **getTableSchema**: 获取指定表的完整Schema信息
                   - 参数: tableName (表英文名称)
                   - 返回: 表的所有字段信息，包括字段名、类型、业务定义等

                2. **getJoinRelations**: 获取多表之间的JOIN关系
                   - 参数: tableNames (表名列表)
                   - 返回: 表之间的JOIN关系，包括父表、子表、关联字段

                ## 工作流程

                1. **理解用户意图**: 仔细分析用户的查询需求，识别关键实体和查询维度

                2. **选择相关表**: 从下面的表列表中，根据业务定义选择最相关的表（1-3张）

                3. **获取表结构**: 使用getTableSchema工具获取所选表的详细字段信息

                4. **确定JOIN关系**: 如果涉及多表查询，使用getJoinRelations工具获取JOIN条件

                5. **生成SQL**:
                   - 只生成SELECT查询，不允许UPDATE/DELETE/DROP等操作
                   - 字段名必须来自getTableSchema的结果
                   - JOIN条件必须基于getJoinRelations的结果
                   - SQL语句符合ODPS语法规范
                   - 表名dd结尾为分区表，分区为全量数据，必须添加时间分区字段限制，格式：yyyyMMdd
                   - 表名di结尾为增量表，分区为每日增量数据。
                   - 分区字段一般默认为dt，可根据字段信息判断。

                6. **返回结果**: 以Markdown格式返回，包含：
                   1.完整的SQL语句，以代码块展示
                   2.使用的表名列表
                   3.SQL语句的中文解释

                ## 约束条件

                - 必须只生成SELECT查询
                - 表名和字段名必须准确，避免错误
                - 对于模糊查询，优先使用LIKE而非等值匹配
                - 注意处理NULL值情况
                - 合理使用聚合函数、分组、排序等

                ## 安全规则

                - 禁止生成任何修改数据的SQL（INSERT/UPDATE/DELETE）
                - 禁止生成任何DDL语句（CREATE/DROP/ALTER）
                - 禁止使用存储过程或系统函数
                
                ## 表信息
                %s

                请根据用户的查询，按照上述流程生成准确的SQL语句。
                """, tablesSummary);
    }
}
