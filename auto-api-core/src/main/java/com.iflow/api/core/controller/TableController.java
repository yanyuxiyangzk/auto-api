package com.iflow.api.core.controller;

import com.iflow.api.core.dto.Result;
import com.iflow.api.core.dto.view.TableInfo;
import com.iflow.api.core.dto.view.TableScanResult;
import com.iflow.api.core.entity.TableSelection;
import com.iflow.api.core.service.ApiGenerationService;
import com.iflow.api.core.service.TableManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 表管理控制器
 * 
 * 引用：REQ-F2-006（手动选择需要生成 API 的表）
 *        REQ-F2-007（全自动模式扫描所有表）
 *        REQ-F2-008（冲突检测，跳过已有 API 的表）
 */
@Slf4j
@RestController
@RequestMapping("/api/table")
public class TableController {

    @Autowired
    private TableManagementService tableManagementService;

    @Autowired
    private ApiGenerationService apiGenerationService;

    /**
     * 扫描数据源获取所有表
     */
    @GetMapping("/scan/{datasourceId}")
    public Result<TableScanResult> scanTables(@PathVariable Long datasourceId) {
        TableScanResult result = tableManagementService.scanTables(datasourceId);
        return Result.success(result);
    }

    /**
     * 获取已选择的表
     */
    @GetMapping("/selected/{datasourceId}")
    public Result<List<TableSelection>> getSelectedTables(@PathVariable Long datasourceId) {
        List<TableSelection> tables = tableManagementService.getSelectedTables(datasourceId);
        return Result.success(tables);
    }

    /**
     * 获取待生成的表
     */
    @GetMapping("/pending/{datasourceId}")
    public Result<List<TableSelection>> getPendingTables(@PathVariable Long datasourceId) {
        List<TableSelection> tables = tableManagementService.getPendingTables(datasourceId);
        return Result.success(tables);
    }

    /**
     * 手动选择表
     */
    @PostMapping("/select/{datasourceId}")
    public Result<Integer> selectTables(
            @PathVariable Long datasourceId,
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> tableNames = (List<String>) request.get("tableNames");
        Boolean selected = (Boolean) request.getOrDefault("selected", true);

        if (tableNames == null || tableNames.isEmpty()) {
            return Result.error("表名列表不能为空");
        }

        int count = tableManagementService.selectTables(datasourceId, tableNames, selected);
        return Result.success(count);
    }

    /**
     * 全选
     */
    @PostMapping("/select/all/{datasourceId}")
    public Result<Integer> selectAll(
            @PathVariable Long datasourceId,
            @RequestParam(defaultValue = "true") boolean skipGenerated) {
        int count = tableManagementService.selectAllTables(datasourceId, true, skipGenerated);
        return Result.success(count);
    }

    /**
     * 取消全选
     */
    @PostMapping("/deselect/all/{datasourceId}")
    public Result<Integer> deselectAll(@PathVariable Long datasourceId) {
        int count = tableManagementService.selectAllTables(datasourceId, false, false);
        return Result.success(count);
    }

    /**
     * 删除选择记录
     */
    @DeleteMapping("/selection/{selectionId}")
    public Result<Void> deleteSelection(@PathVariable Long selectionId) {
        tableManagementService.deleteSelection(selectionId);
        return Result.success();
    }

    /**
     * 清空选择记录
     */
    @DeleteMapping("/clear/{datasourceId}")
    public Result<Void> clearSelections(@PathVariable Long datasourceId) {
        tableManagementService.clearSelections(datasourceId);
        return Result.success();
    }

    /**
     * 获取单个表信息
     */
    @GetMapping("/info/{datasourceId}/{tableName}")
    public Result<TableInfo> getTableInfo(
            @PathVariable Long datasourceId,
            @PathVariable String tableName) {
        TableInfo info = tableManagementService.getTableInfo(datasourceId, tableName);
        return Result.success(info);
    }

    /**
     * 检查表名是否冲突
     */
    @GetMapping("/check/conflict/{datasourceId}/{tableName}")
    public Result<Boolean> checkConflict(
            @PathVariable Long datasourceId,
            @PathVariable String tableName) {
        boolean conflict = tableManagementService.isTableConflict(datasourceId, tableName);
        return Result.success(conflict);
    }

    /**
     * 批量获取表信息
     */
    @PostMapping("/info/batch/{datasourceId}")
    public Result<List<TableInfo>> getTablesInfo(
            @PathVariable Long datasourceId,
            @RequestBody List<String> tableNames) {
        List<TableInfo> infos = tableManagementService.getTablesInfo(datasourceId, tableNames);
        return Result.success(infos);
    }

    /**
     * 获取数据源统计信息
     */
    @GetMapping("/statistics/{datasourceId}")
    public Result<Map<String, Object>> getStatistics(@PathVariable Long datasourceId) {
        Map<String, Object> stats = tableManagementService.getStatistics(datasourceId);
        return Result.success(stats);
    }

    /**
     * 生成 API（单表）
     */
    @PostMapping("/generate/{selectionId}")
    public Result<Void> generateApi(@PathVariable Long selectionId) {
        apiGenerationService.regenerateApi(selectionId);
        return Result.success();
    }

    /**
     * 批量生成 API
     */
    @PostMapping("/generate/batch")
    public Result<Integer> batchGenerate(@RequestBody List<Long> selectionIds) {
        int count = apiGenerationService.batchGenerate(selectionIds);
        return Result.success(count);
    }

    /**
     * 删除 API
     */
    @DeleteMapping("/api/{selectionId}")
    public Result<Void> deleteApi(@PathVariable Long selectionId) {
        apiGenerationService.deleteApi(selectionId);
        return Result.success();
    }

    /**
     * 扫描并生成 API（全自动模式）
     */
    @PostMapping("/scan-and-generate/{datasourceId}")
    public Result<Integer> scanAndGenerate(
            @PathVariable Long datasourceId,
            @RequestBody(required = false) Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> tableNames = request != null ? 
            (List<String>) request.get("tableNames") : null;
        Boolean autoSelect = request != null ? 
            (Boolean) request.getOrDefault("autoSelect", true) : true;

        int count = apiGenerationService.scanAndGenerate(datasourceId, tableNames, autoSelect);
        return Result.success(count);
    }

    /**
     * 刷新所有 API（热更新）
     */
    @PostMapping("/refresh-all")
    public Result<Void> refreshAllApis() {
        apiGenerationService.refreshAllApis();
        return Result.success();
    }
}
