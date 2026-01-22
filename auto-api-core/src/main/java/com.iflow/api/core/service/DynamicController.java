package com.iflow.api.core.service;

import com.iflow.api.core.dto.Result;
import com.iflow.api.core.dto.metadata.ColumnMeta;
import com.iflow.api.core.dto.metadata.TableMeta;
import com.iflow.api.core.datasource.DynamicDataSourceService;
import com.iflow.api.core.util.NamingConverter;
import com.iflow.api.core.util.TypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 动态 API 控制器
 * 
 * 引用：REQ-F2-001（通用的 CRUD API）
 *        REQ-F2-005（返回格式统一为 JSON）
 */
@Slf4j
@RestController
@RequestMapping("/api/dynamic")
public class DynamicController {

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    @Autowired
    private MetadataService metadataService;

    /**
     * 通用的请求处理入口
     */
    @RequestMapping(value = "/{tablePath}", method = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE
    })
    public Result<?> handleRequest(
            @PathVariable String tablePath,
            @RequestParam(required = false) Object id,
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {

        String method = request.getMethod();
        String tableName = NamingConverter.toSnakeCase(tablePath.replace("-", "_"));

        switch (method) {
            case "GET":
                if (id != null) {
                    return getById(tableName, id);
                } else {
                    return list(tableName, body);
                }
            case "POST":
                return create(tableName, body);
            case "PUT":
                return update(tableName, id, body);
            case "DELETE":
                return delete(tableName, id);
            default:
                return Result.error("不支持的请求方法: " + method);
        }
    }

    /**
     * 查询列表
     */
    public Result<?> list(String tableName, Map<String, Object> params) {
        Long datasourceId = getDatasourceIdByTable(tableName);
        if (datasourceId == null) {
            return Result.error("表不存在: " + tableName);
        }

        try {
            TableMeta tableMeta = metadataService.getTableDetail(datasourceId, tableName);
            String sql = buildSelectSql(tableMeta, params);
            
            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

            // 转换字段名为驼峰命名
            List<Map<String, Object>> convertedRows = rows.stream()
                .map(this::convertToCamelCase)
                .collect(Collectors.toList());

            return Result.success(convertedRows);

        } catch (Exception e) {
            log.error("查询列表失败: table={}", tableName, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询详情
     */
    public Result<?> getById(String tableName, Object id) {
        Long datasourceId = getDatasourceIdByTable(tableName);
        if (datasourceId == null) {
            return Result.error("表不存在: " + tableName);
        }

        try {
            TableMeta tableMeta = metadataService.getTableDetail(datasourceId, tableName);
            String primaryKey = tableMeta.getPrimaryKeys().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("表没有主键: " + tableName));

            String sql = "SELECT * FROM " + tableName + " WHERE " + primaryKey + " = ?";

            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            Map<String, Object> row = jdbcTemplate.queryForMap(sql, id);

            // 转换字段名为驼峰命名
            Map<String, Object> convertedRow = convertToCamelCase(row);

            return Result.success(convertedRow);

        } catch (Exception e) {
            log.error("查询详情失败: table={}, id={}", tableName, id, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增数据
     */
    public Result<?> create(String tableName, Map<String, Object> data) {
        Long datasourceId = getDatasourceIdByTable(tableName);
        if (datasourceId == null) {
            return Result.error("表不存在: " + tableName);
        }

        try {
            // 转换字段名为下划线命名
            Map<String, Object> snakeCaseData = convertToSnakeCase(data);

            String columns = String.join(", ", snakeCaseData.keySet());
            String placeholders = snakeCaseData.keySet().stream()
                .map(k -> "?")
                .collect(Collectors.joining(", "));

            String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";

            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            jdbcTemplate.update(sql, snakeCaseData.values().toArray());

            return Result.success("创建成功");

        } catch (Exception e) {
            log.error("新增失败: table={}", tableName, e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新数据
     */
    public Result<?> update(String tableName, Object id, Map<String, Object> data) {
        Long datasourceId = getDatasourceIdByTable(tableName);
        if (datasourceId == null) {
            return Result.error("表不存在: " + tableName);
        }

        try {
            TableMeta tableMeta = metadataService.getTableDetail(datasourceId, tableName);
            String primaryKey = tableMeta.getPrimaryKeys().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("表没有主键: " + tableName));

            // 转换字段名为下划线命名
            Map<String, Object> snakeCaseData = convertToSnakeCase(data);

            String setClause = snakeCaseData.keySet().stream()
                .map(k -> k + " = ?")
                .collect(Collectors.joining(", "));

            String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + primaryKey + " = ?";

            List<Object> params = new ArrayList<>(snakeCaseData.values());
            params.add(id);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            int affected = jdbcTemplate.update(sql, params.toArray());

            if (affected > 0) {
                return Result.success("更新成功");
            } else {
                return Result.error("记录不存在");
            }

        } catch (Exception e) {
            log.error("更新失败: table={}, id={}", tableName, id, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除数据
     */
    public Result<?> delete(String tableName, Object id) {
        Long datasourceId = getDatasourceIdByTable(tableName);
        if (datasourceId == null) {
            return Result.error("表不存在: " + tableName);
        }

        try {
            TableMeta tableMeta = metadataService.getTableDetail(datasourceId, tableName);
            String primaryKey = tableMeta.getPrimaryKeys().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("表没有主键: " + tableName));

            String sql = "DELETE FROM " + tableName + " WHERE " + primaryKey + " = ?";

            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            int affected = jdbcTemplate.update(sql, id);

            if (affected > 0) {
                return Result.success("删除成功");
            } else {
                return Result.error("记录不存在");
            }

        } catch (Exception e) {
            log.error("删除失败: table={}, id={}", tableName, id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 构建查询 SQL
     */
    private String buildSelectSql(TableMeta tableMeta, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableMeta.getName());

        // WHERE 条件
        if (params != null && !params.isEmpty()) {
            List<String> conditions = new ArrayList<>();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String column = NamingConverter.toSnakeCase(entry.getKey());
                if (entry.getValue() == null) {
                    conditions.add(column + " IS NULL");
                } else {
                    conditions.add(column + " = ?");
                }
            }
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        // LIMIT
        sql.append(" LIMIT 100");

        return sql.toString();
    }

    /**
     * 将 Map 的键从下划线命名转换为驼峰命名
     */
    private Map<String, Object> convertToCamelCase(Map<String, Object> map) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String camelKey = NamingConverter.toCamelCase(entry.getKey());
            result.put(camelKey, entry.getValue());
        }
        return result;
    }

    /**
     * 将 Map 的键从驼峰命名转换为下划线命名
     */
    private Map<String, Object> convertToSnakeCase(Map<String, Object> map) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String snakeKey = NamingConverter.toSnakeCase(entry.getKey());
            result.put(snakeKey, entry.getValue());
        }
        return result;
    }

    /**
     * 根据表名获取数据源 ID（简化版本，实际应查询表选择记录）
     */
    private Long getDatasourceIdByTable(String tableName) {
        // TODO: 从 TableSelection 或 ApiGenerationStatus 中查找
        // 临时返回默认数据源 ID
        return 1L;
    }

    /**
     * 获取表结构信息
     */
    @GetMapping("/{tablePath}/schema")
    public Result<?> getSchema(@PathVariable String tablePath) {
        String tableName = NamingConverter.toSnakeCase(tablePath.replace("-", "_"));
        Long datasourceId = getDatasourceIdByTable(tableName);
        
        if (datasourceId == null) {
            return Result.error("表不存在: " + tableName);
        }

        try {
            TableMeta tableMeta = metadataService.getTableDetail(datasourceId, tableName);
            return Result.success(tableMeta);
        } catch (Exception e) {
            log.error("获取表结构失败: table={}", tableName, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 批量操作
     */
    @PostMapping("/{tablePath}/batch")
    public Result<?> batchOperation(
            @PathVariable String tablePath,
            @RequestBody Map<String, Object> body) {
        
        String tableName = NamingConverter.toSnakeCase(tablePath.replace("-", "_"));
        String operation = (String) body.get("operation");
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");

        if (data == null || data.isEmpty()) {
            return Result.error("数据不能为空");
        }

        switch (operation) {
            case "create":
                for (Map<String, Object> item : data) {
                    create(tableName, item);
                }
                return Result.success("批量创建成功");
                
            case "update":
                for (Map<String, Object> item : data) {
                    Object id = item.remove("id");
                    update(tableName, id, item);
                }
                return Result.success("批量更新成功");
                
            case "delete":
                for (Map<String, Object> item : data) {
                    Object id = item.get("id");
                    delete(tableName, id);
                }
                return Result.success("批量删除成功");
                
            default:
                return Result.error("不支持的操作: " + operation);
        }
    }
}
