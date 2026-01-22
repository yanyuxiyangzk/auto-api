package com.iflow.api.core.controller;

import com.iflow.api.core.dto.Result;
import com.iflow.api.core.entity.ApiGenerationStatus;
import com.iflow.api.core.service.ApiGenerationService;
import com.iflow.api.core.service.DynamicRouteRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API 管理控制器
 * 
 * 引用：REQ-F2-004（API 状态管理）
 */
@Slf4j
@RestController
@RequestMapping("/api/api")
public class ApiController {

    @Autowired
    private ApiGenerationService apiGenerationService;

    @Autowired
    private DynamicRouteRegistry dynamicRouteRegistry;

    /**
     * 获取所有已生成的 API
     */
    @GetMapping("/list")
    public Result<List<ApiGenerationStatus>> listAll() {
        List<ApiGenerationStatus> apis = apiGenerationService.getAllGeneratedApis();
        return Result.success(apis);
    }

    /**
     * 获取数据源的 API 列表
     */
    @GetMapping("/list/{datasourceId}")
    public Result<List<ApiGenerationStatus>> listByDatasource(@PathVariable Long datasourceId) {
        List<ApiGenerationStatus> apis = apiGenerationService.getGeneratedApisByDatasource(datasourceId);
        return Result.success(apis);
    }

    /**
     * 获取单个 API 状态
     */
    @GetMapping("/status/{tableSelectionId}")
    public Result<ApiGenerationStatus> getStatus(@PathVariable Long tableSelectionId) {
        return apiGenerationService.getApiStatus(tableSelectionId)
            .map(Result::success)
            .orElse(Result.error("API 不存在"));
    }

    /**
     * 重新生成 API
     */
    @PostMapping("/regenerate/{tableSelectionId}")
    public Result<Void> regenerate(@PathVariable Long tableSelectionId) {
        apiGenerationService.regenerateApi(tableSelectionId);
        return Result.success();
    }

    /**
     * 删除 API
     */
    @DeleteMapping("/delete/{tableSelectionId}")
    public Result<Void> delete(@PathVariable Long tableSelectionId) {
        apiGenerationService.deleteApi(tableSelectionId);
        return Result.success();
    }

    /**
     * 刷新所有 API（热更新）
     */
    @PostMapping("/refresh-all")
    public Result<Void> refreshAll() {
        apiGenerationService.refreshAllApis();
        return Result.success();
    }

    /**
     * 获取所有注册的路由
     */
    @GetMapping("/routes")
    public Result<Map<String, String>> getRoutes() {
        Map<String, String> routes = dynamicRouteRegistry.getAllRouteMappings().entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().getMethod().getName()
            ));
        return Result.success(routes);
    }

    /**
     * 检查表是否已生成 API
     */
    @GetMapping("/check/{datasourceId}/{tableName}")
    public Result<Boolean> checkApiGenerated(
            @PathVariable Long datasourceId,
            @PathVariable String tableName) {
        boolean generated = apiGenerationService.isApiGenerated(tableName, datasourceId);
        return Result.success(generated);
    }
}
