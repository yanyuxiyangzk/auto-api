package com.iflow.api.core.controller;

import com.iflow.api.core.dto.Result;
import com.iflow.api.core.datasource.DynamicDataSourceService;
import com.iflow.api.core.entity.DatasourceConfig;
import com.iflow.api.core.repository.DatasourceConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 数据源管理控制器
 * 
 * 引用：REQ-F4-001（管理多个数据源配置）
 *        REQ-F4-002（动态添加/删除第三方数据源）
 */
@Slf4j
@RestController
@RequestMapping("/api/datasource")
public class DatasourceController {

    @Autowired
    private DatasourceConfigRepository datasourceConfigRepository;

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    /**
     * 获取所有数据源
     */
    @GetMapping("/list")
    public Result<List<DatasourceConfig>> list() {
        List<DatasourceConfig> configs = datasourceConfigRepository.findAllActive();
        return Result.success(configs);
    }

    /**
     * 获取所有数据源（包括已删除的）
     */
    @GetMapping("/list/all")
    public Result<List<DatasourceConfig>> listAll() {
        List<DatasourceConfig> configs = datasourceConfigRepository.selectList(null);
        return Result.success(configs);
    }

    /**
     * 获取数据源详情
     */
    @GetMapping("/{id}")
    public Result<DatasourceConfig> getById(@PathVariable Long id) {
        Optional<DatasourceConfig> config = datasourceConfigRepository.findByIdWithoutDeleted(id);
        return config.map(Result::success)
            .orElse(Result.error("数据源不存在: " + id));
    }

    /**
     * 创建数据源
     */
    @PostMapping("/create")
    public Result<DatasourceConfig> create(@RequestBody DatasourceConfig config) {
        // 检查名称是否重复
        if (datasourceConfigRepository.existsByName(config.getName())) {
            return Result.error("数据源名称已存在: " + config.getName());
        }

        // 测试连接
        if (!dynamicDataSourceService.testConnection(config)) {
            return Result.error("数据源连接测试失败，请检查配置");
        }

        config.setStatus(1);
        datasourceConfigRepository.insert(config);
        DatasourceConfig saved = config;
        log.info("数据源创建成功: id={}, name={}", saved.getId(), saved.getName());
        return Result.success(saved);
    }

    /**
     * 更新数据源
     */
    @PutMapping("/update/{id}")
    public Result<DatasourceConfig> update(@PathVariable Long id, @RequestBody DatasourceConfig config) {
        Optional<DatasourceConfig> existing = datasourceConfigRepository.findByIdWithoutDeleted(id);
        if (existing.isEmpty()) {
            return Result.error("数据源不存在: " + id);
        }

        DatasourceConfig oldConfig = existing.get();
        
        // 如果密码为空，保留原密码
        if (config.getPassword() == null || config.getPassword().isEmpty()) {
            config.setPassword(oldConfig.getPassword());
        }

        // 测试新连接
        if (!dynamicDataSourceService.testConnection(config)) {
            return Result.error("数据源连接测试失败，请检查配置");
        }

        config.setId(id);
        config.setStatus(oldConfig.getStatus());
        datasourceConfigRepository.updateById(config);
        DatasourceConfig saved = config;
        log.info("数据源更新成功: id={}", id);
        return Result.success(saved);
    }

    /**
     * 删除数据源（软删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Optional<DatasourceConfig> existing = datasourceConfigRepository.findByIdWithoutDeleted(id);
        if (existing.isEmpty()) {
            return Result.error("数据源不存在: " + id);
        }

        DatasourceConfig config = existing.get();
        config.setStatus(0); // 标记为删除
        datasourceConfigRepository.updateById(config);

        // 关闭数据源连接
        dynamicDataSourceService.closeDataSource(id);
        log.info("数据源已删除: id={}, name={}", id, config.getName());
        return Result.success();
    }

    /**
     * 硬删除数据源
     */
    @DeleteMapping("/delete/hard/{id}")
    public Result<Void> hardDelete(@PathVariable Long id) {
        if (!datasourceConfigRepository.existsByIdWithoutDeleted(id)) {
            return Result.error("数据源不存在: " + id);
        }

        datasourceConfigRepository.deleteById(id);
        dynamicDataSourceService.closeDataSource(id);
        log.info("数据源已硬删除: id={}", id);
        return Result.success();
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/test")
    public Result<Boolean> test(@RequestBody DatasourceConfig config) {
        boolean success = dynamicDataSourceService.testConnection(config);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error("连接测试失败");
        }
    }

    /**
     * 刷新数据源连接
     */
    @PostMapping("/refresh/{id}")
    public Result<Void> refresh(@PathVariable Long id) {
        if (!datasourceConfigRepository.existsByIdWithoutDeleted(id)) {
            return Result.error("数据源不存在: " + id);
        }

        dynamicDataSourceService.refreshDataSource(id);
        log.info("数据源已刷新: id={}", id);
        return Result.success();
    }

    /**
     * 关闭数据源连接
     */
    @PostMapping("/close/{id}")
    public Result<Void> close(@PathVariable Long id) {
        if (!datasourceConfigRepository.existsByIdWithoutDeleted(id)) {
            return Result.error("数据源不存在: " + id);
        }

        dynamicDataSourceService.closeDataSource(id);
        log.info("数据源连接已关闭: id={}", id);
        return Result.success();
    }

    /**
     * 获取数据源连接池状态
     */
    @GetMapping("/status/{id}")
    public Result<Object> getStatus(@PathVariable Long id) {
        if (!datasourceConfigRepository.existsByIdWithoutDeleted(id)) {
            return Result.error("数据源不存在: " + id);
        }

        Object status = dynamicDataSourceService.getOrCreateDataSource(id).getDataSourceStat();
        return Result.success(status);
    }

    /**
     * 检查名称是否重复
     */
    @GetMapping("/check/name")
    public Result<Boolean> checkName(@RequestParam String name) {
        boolean exists = datasourceConfigRepository.existsByName(name);
        return Result.success(!exists);
    }
}
