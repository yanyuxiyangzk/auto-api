package com.iflow.api.core.service;

import com.iflow.api.core.dto.metadata.TableMeta;
import com.iflow.api.core.datasource.DynamicDataSourceService;
import com.iflow.api.core.entity.ApiGenerationStatus;
import com.iflow.api.core.entity.DatasourceConfig;
import com.iflow.api.core.entity.TableSelection;
import com.iflow.api.core.graphql.GraphQLMutationResolver;
import com.iflow.api.core.graphql.GraphQLQueryResolver;
import com.iflow.api.core.graphql.GraphQLSchemaGenerator;
import graphql.schema.GraphQLSchema;
import com.iflow.api.core.repository.ApiGenerationStatusRepository;
import com.iflow.api.core.repository.DatasourceConfigRepository;
import com.iflow.api.core.repository.TableSelectionRepository;
import com.iflow.api.core.util.NamingConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * API 生成服务（整合层）
 * 
 * 引用：REQ-F2-001（生成 RESTful API）
 *        REQ-F1-001（生成 GraphQL API）
 *        REQ-F4-003（扫描数据源获取表信息）
 */
@Slf4j
@Service
public class ApiGenerationService {

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    @Autowired
    private DynamicRouteRegistry dynamicRouteRegistry;

    @Autowired
    private GraphQLSchemaGenerator graphQLSchemaGenerator;

    @Autowired
    private GraphQLQueryResolver graphQLQueryResolver;

    @Autowired
    private GraphQLMutationResolver graphQLMutationResolver;

    @Autowired
    private DatasourceConfigRepository datasourceConfigRepository;

    @Autowired
    private TableSelectionRepository tableSelectionRepository;

    @Autowired
    private ApiGenerationStatusRepository apiGenerationStatusRepository;

    /**
     * 扫描数据源生成 API
     */
    @Transactional
    public int scanAndGenerate(Long datasourceId, List<String> tableNames, boolean autoSelect) {
        log.info("开始扫描数据源生成 API");
        
        // 获取数据源配置
        DatasourceConfig config = datasourceConfigRepository
            .findByIdWithoutDeleted(datasourceId)
            .orElseThrow(() -> new IllegalArgumentException("数据源不存在: " + datasourceId));

        // 扫描所有表
        List<TableMeta> allTables = metadataService.getAllTables(datasourceId);

        // 过滤需要生成的表
        List<TableMeta> targetTables;
        if (tableNames == null || tableNames.isEmpty()) {
            targetTables = allTables;
        } else {
            Set<String> nameSet = new HashSet<>(tableNames);
            targetTables = allTables.stream()
                .filter(t -> nameSet.contains(t.getTableName()))
                .collect(Collectors.toList());
        }

        int generatedCount = 0;
        for (TableMeta table : targetTables) {
            try {
                // 创建表选择记录
                TableSelection selection = new TableSelection();
                selection.setDatasourceId(datasourceId);
                selection.setTableName(table.getTableName());
                selection.setSelected(true);
                selection.setGenerateMode(autoSelect ? "auto" : "manual");
                tableSelectionRepository.insert(selection);

                // 生成 API
                generateApiForTable(selection, table);
                generatedCount++;
            } catch (Exception e) {
                log.error("生成 API 失败: table={}", table.getTableName(), e);
            }
        }

        log.info("API 生成完成: success={}", generatedCount);
        return generatedCount;
    }

    /**
     * 为单个表生成 API
     */
    @Transactional
    public void generateApiForTable(TableSelection selection, TableMeta tableMeta) {
        Long datasourceId = selection.getDatasourceId();
        String tableName = selection.getTableName();

        log.info("开始生成 API: table={}", tableName);

        // 生成 REST API 路由
        dynamicRouteRegistry.registerRestApi(tableMeta, datasourceId);

        // 生成 GraphQL 类型
        graphQLSchemaGenerator.generateType(tableMeta);

        // 创建 API 生成状态
        ApiGenerationStatus status = new ApiGenerationStatus();
        status.setDatasourceId(datasourceId);
        status.setTableName(tableName);
        status.setStatus("generated");
        apiGenerationStatusRepository.insert(status);

        log.info("API 生成成功: table={}", tableName);
    }

    /**
     * 重新生成 API
     */
    @Transactional
    public void regenerateApi(Long tableSelectionId) {
        log.info("重新生成 API: tableSelectionId={}", tableSelectionId);
    }

    /**
     * 删除 API
     */
    @Transactional
    public void deleteApi(Long tableSelectionId) {
        log.info("删除 API: tableSelectionId={}", tableSelectionId);
    }

    /**
     * 批量生成 API
     */
    @Transactional
    public int batchGenerate(List<Long> selectionIds) {
        return 0;
    }

    /**
     * 获取所有已生成的 API
     */
    public List<ApiGenerationStatus> getAllGeneratedApis() {
        return Collections.emptyList();
    }

    /**
     * 获取数据源已生成的 API
     */
    public List<ApiGenerationStatus> getGeneratedApisByDatasource(Long datasourceId) {
        return Collections.emptyList();
    }

    /**
     * 获取单个 API 状态
     */
    public Optional<ApiGenerationStatus> getApiStatus(Long tableSelectionId) {
        return Optional.empty();
    }

    /**
     * 获取 GraphQL Schema
     */
    public GraphQLSchema getGraphQLSchema(Long datasourceId) {
        return graphQLSchemaGenerator.generateSchema(Collections.emptyList());
    }

    /**
     * 检查表是否已生成 API
     */
    public boolean isApiGenerated(String tableName, Long datasourceId) {
        return false;
    }

    /**
     * 刷新所有 API（热更新）
     */
    @Transactional
    public void refreshAllApis() {
        log.info("刷新所有 API");
    }
}
