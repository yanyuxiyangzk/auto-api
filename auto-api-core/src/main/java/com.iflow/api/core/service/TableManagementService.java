package com.iflow.api.core.service;

import com.iflow.api.core.dto.metadata.ColumnMeta;
import com.iflow.api.core.dto.metadata.TableMeta;
import com.iflow.api.core.dto.view.TableInfo;
import com.iflow.api.core.dto.view.TableScanResult;
import com.iflow.api.core.datasource.DynamicDataSourceService;
import com.iflow.api.core.entity.ApiGenerationStatus;
import com.iflow.api.core.entity.DatasourceConfig;
import com.iflow.api.core.entity.TableSelection;
import com.iflow.api.core.repository.ApiGenerationStatusRepository;
import com.iflow.api.core.repository.DatasourceConfigRepository;
import com.iflow.api.core.repository.TableSelectionRepository;
import com.iflow.api.core.util.NamingConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表管理服务
 * 
 * 引用：REQ-F2-006（手动选择需要生成 API 的表）
 *        REQ-F2-007（全自动模式扫描所有表）
 *        REQ-F2-008（冲突检测，跳过已有 API 的表）
 */
@Slf4j
@Service
public class TableManagementService {

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    @Autowired
    private DatasourceConfigRepository datasourceConfigRepository;

    @Autowired
    private TableSelectionRepository tableSelectionRepository;

    @Autowired
    private ApiGenerationStatusRepository apiGenerationStatusRepository;

    /**
     * 扫描数据源获取所有表
     * 
     * @param datasourceId 数据源 ID
     * @return 扫描结果
     */
    public TableScanResult scanTables(Long datasourceId) {
        log.info("开始扫描数据源表: datasourceId={}", datasourceId);

        // 获取数据源配置
        DatasourceConfig config = datasourceConfigRepository
            .findByIdWithoutDeleted(datasourceId)
            .orElseThrow(() -> new IllegalArgumentException("数据源不存在: " + datasourceId));

        // 获取所有表
        List<TableMeta> allTables = metadataService.getAllTables(datasourceId);

        // 获取已选择的表
        List<TableSelection> selectedTables = tableSelectionRepository
            .findByDatasourceIdAndSelected(datasourceId, true);

        // 获取已生成 API 的表
        List<ApiGenerationStatus> generatedApis = apiGenerationStatusRepository
            .findByDatasourceIdAndStatus(datasourceId, "ACTIVE");

        Set<String> selectedTableNames = selectedTables.stream()
            .map(TableSelection::getTableName)
            .collect(Collectors.toSet());

        Set<String> generatedTableNames = generatedApis.stream()
            .map(ApiGenerationStatus::getTableName)
            .collect(Collectors.toSet());

        // 构建表信息列表
        List<TableInfo> tables = new ArrayList<>();
        int conflictCount = 0;

        for (TableMeta table : allTables) {
            TableInfo info = convertToTableInfo(table);
            info.setSelected(selectedTableNames.contains(table.getName()));
            info.setApiGenerated(generatedTableNames.contains(table.getName()));

            if (generatedTableNames.contains(table.getName())) {
                info.setConflict(true);
                conflictCount++;
            }

            tables.add(info);
        }

        // 构建扫描结果
        TableScanResult result = new TableScanResult();
        result.setDatasourceId(datasourceId);
        result.setDatasourceName(config.getName());
        result.setTotalCount(allTables.size());
        result.setSelectedCount(selectedTableNames.size());
        result.setGeneratedCount(generatedTableNames.size());
        result.setConflictCount(conflictCount);
        result.setTables(tables);
        result.setScanTime(new Date());

        log.info("数据源扫描完成: datasourceId={}, total={}, selected={}, generated={}", 
            datasourceId, allTables.size(), selectedTableNames.size(), generatedTableNames.size());

        return result;
    }

    /**
     * 手动选择表
     * 
     * @param datasourceId 数据源 ID
     * @param tableNames 表名列表
     * @param selected 是否选择
     * @return 选择的表数量
     */
    @Transactional
    public int selectTables(Long datasourceId, List<String> tableNames, boolean selected) {
        log.info("手动选择表: datasourceId={}, tableNames={}, selected={}", 
            datasourceId, tableNames, selected);

        List<TableMeta> allTables = metadataService.getAllTables(datasourceId);
        Set<String> tableNameSet = new HashSet<>(tableNames);

        int count = 0;
        for (TableMeta table : allTables) {
            if (tableNameSet.contains(table.getName())) {
                // 检查是否已存在选择记录
                Optional<TableSelection> existing = tableSelectionRepository
                    .findByDatasourceIdAndTableName(datasourceId, table.getName());

                if (existing.isPresent()) {
                    // 更新现有记录
                    TableSelection selection = existing.get();
                    selection.setSelected(selected);
                    tableSelectionRepository.updateById(selection);
                } else {
                    // 创建新记录
                    TableSelection selection = new TableSelection();
                    selection.setDatasourceId(datasourceId);
                    selection.setTableName(table.getName());
                    selection.setSelected(selected);
                    selection.setGenerateMode("manual");
                    tableSelectionRepository.insert(selection);
                }
                count++;
            }
        }

        log.info("表选择完成: datasourceId={}, count={}", datasourceId, count);
        return count;
    }

    /**
     * 全选/取消全选
     * 
     * @param datasourceId 数据源 ID
     * @param selected 是否选择
     * @param skipGenerated 是否跳过已生成 API 的表
     * @return 处理的表数量
     */
    @Transactional
    public int selectAllTables(Long datasourceId, boolean selected, boolean skipGenerated) {
        log.info("全选/取消全选: datasourceId={}, selected={}, skipGenerated={}", 
            datasourceId, selected, skipGenerated);

        List<TableMeta> allTables = metadataService.getAllTables(datasourceId);
        Set<String> generatedTableNames = new HashSet<>();

        if (skipGenerated) {
            List<ApiGenerationStatus> generatedApis = apiGenerationStatusRepository
                .findByDatasourceIdAndStatus(datasourceId, "ACTIVE");
            generatedTableNames = generatedApis.stream()
                .map(ApiGenerationStatus::getTableName)
                .collect(Collectors.toSet());
        }

        int count = 0;
        for (TableMeta table : allTables) {
            if (skipGenerated && generatedTableNames.contains(table.getName())) {
                continue;
            }

            Optional<TableSelection> existing = tableSelectionRepository
                .findByDatasourceIdAndTableName(datasourceId, table.getName());

            if (existing.isPresent()) {
                TableSelection selection = existing.get();
                selection.setSelected(selected);
                tableSelectionRepository.updateById(selection);
            } else {
                TableSelection selection = new TableSelection();
                selection.setDatasourceId(datasourceId);
                selection.setTableName(table.getName());
                selection.setSelected(selected);
                selection.setGenerateMode(skipGenerated ? "auto" : "manual");
                tableSelectionRepository.insert(selection);
            }
            count++;
        }

        log.info("全选/取消全选完成: datasourceId={}, count={}", datasourceId, count);
        return count;
    }

    /**
     * 获取已选择的表
     * 
     * @param datasourceId 数据源 ID
     * @return 表选择记录列表
     */
    public List<TableSelection> getSelectedTables(Long datasourceId) {
        return tableSelectionRepository.findByDatasourceIdAndSelected(datasourceId, true);
    }

    /**
     * 获取待生成的表（已选择但未生成 API）
     * 
     * @param datasourceId 数据源 ID
     * @return 表选择记录列表
     */
    public List<TableSelection> getPendingTables(Long datasourceId) {
        List<TableSelection> selectedTables = getSelectedTables(datasourceId);
        Set<String> generatedTableNames = apiGenerationStatusRepository
            .findByDatasourceIdAndStatus(datasourceId, "ACTIVE")
            .stream()
            .map(ApiGenerationStatus::getTableName)
            .collect(Collectors.toSet());

        return selectedTables.stream()
            .filter(t -> !generatedTableNames.contains(t.getTableName()))
            .collect(Collectors.toList());
    }

    /**
     * 删除表选择记录
     * 
     * @param selectionId 选择记录 ID
     */
    @Transactional
    public void deleteSelection(Long selectionId) {
        TableSelection selection = tableSelectionRepository.selectById(selectionId);
        if (selection == null) {
            throw new IllegalArgumentException("选择记录不存在: " + selectionId);
        }

        tableSelectionRepository.deleteById(selectionId);
        log.info("表选择记录已删除: id={}, table={}", selectionId, selection.getTableName());
    }

    /**
     * 清空数据源的所有选择记录
     * 
     * @param datasourceId 数据源 ID
     */
    @Transactional
    public void clearSelections(Long datasourceId) {
        List<TableSelection> selections = tableSelectionRepository
            .findByDatasourceId(datasourceId);
        for (TableSelection selection : selections) {
            tableSelectionRepository.deleteById(selection.getId());
        }
        log.info("数据源选择记录已清空: datasourceId={}", datasourceId);
    }

    /**
     * 获取表的详细信息
     * 
     * @param datasourceId 数据源 ID
     * @param tableName 表名
     * @return 表信息
     */
    public TableInfo getTableInfo(Long datasourceId, String tableName) {
        TableMeta tableMeta = metadataService.getTableDetail(datasourceId, tableName);
        return convertToTableInfo(tableMeta);
    }

    /**
     * 检查表名是否冲突
     * 
     * @param datasourceId 数据源 ID
     * @param tableName 表名
     * @return true-冲突, false-无冲突
     */
    public boolean isTableConflict(Long datasourceId, String tableName) {
        return apiGenerationStatusRepository
            .findByDatasourceIdAndStatus(datasourceId, "ACTIVE")
            .stream()
            .anyMatch(s -> s.getTableName().equals(tableName));
    }

    /**
     * 批量获取表信息
     * 
     * @param datasourceId 数据源 ID
     * @param tableNames 表名列表
     * @return 表信息列表
     */
    public List<TableInfo> getTablesInfo(Long datasourceId, List<String> tableNames) {
        return tableNames.stream()
            .map(name -> getTableInfo(datasourceId, name))
            .collect(Collectors.toList());
    }

    /**
     * 转换 TableMeta 为 TableInfo
     */
    private TableInfo convertToTableInfo(TableMeta tableMeta) {
        TableInfo info = new TableInfo();
        info.setName(tableMeta.getName());
        info.setRemarks(tableMeta.getRemarks());
        info.setType(tableMeta.getType());
        info.setCatalog(tableMeta.getCatalog());
        info.setSchema(tableMeta.getSchema());
        info.setColumnCount(tableMeta.getColumns().size());
        info.setColumns(tableMeta.getColumns().stream()
            .map(this::convertToColumnBrief)
            .collect(Collectors.toList()));
        return info;
    }

    /**
     * 转换 ColumnMeta 为 ColumnBrief
     */
    private TableInfo.ColumnBrief convertToColumnBrief(ColumnMeta column) {
        TableInfo.ColumnBrief brief = new TableInfo.ColumnBrief();
        brief.setName(column.getName());
        brief.setType(column.getTypeName());
        brief.setIsPrimaryKey(column.isPrimaryKey());
        brief.setIsNullable(column.isNullable());
        brief.setComment(column.getComment());
        return brief;
    }

    /**
     * 获取数据源统计信息
     * 
     * @param datasourceId 数据源 ID
     * @return 统计信息
     */
    public Map<String, Object> getStatistics(Long datasourceId) {
        Map<String, Object> stats = new HashMap<>();

        List<TableMeta> allTables = metadataService.getAllTables(datasourceId);
        List<TableSelection> selections = tableSelectionRepository.findByDatasourceId(datasourceId);
        List<ApiGenerationStatus> apis = apiGenerationStatusRepository
            .findByDatasourceIdAndStatus(datasourceId, "ACTIVE");

        stats.put("totalTables", allTables.size());
        stats.put("selectedTables", selections.stream().filter(s -> Boolean.TRUE.equals(s.getSelected())).count());
        stats.put("generatedApis", apis.size());
        stats.put("pendingTables", getPendingTables(datasourceId).size());
        stats.put("databaseVersion", metadataService.getDatabaseVersion(datasourceId));
        stats.put("databaseType", metadataService.getDatabaseType(datasourceId));

        return stats;
    }
}
