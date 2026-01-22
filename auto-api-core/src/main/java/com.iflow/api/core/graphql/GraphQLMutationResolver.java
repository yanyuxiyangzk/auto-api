package com.iflow.api.core.graphql;

import com.iflow.api.core.datasource.DynamicDataSourceService;
import com.iflow.api.core.dto.metadata.TableMeta;
import com.iflow.api.core.service.MetadataService;
import com.iflow.api.core.util.NamingConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GraphQL Mutation Resolver
 * 
 * 引用：REQ-F1-005（自动生成 Mutation API）
 */
@Slf4j
@Component
public class GraphQLMutationResolver {

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    @Autowired
    private MetadataService metadataService;

    /**
     * 创建记录
     * 
     * @param typeName 类型名称
     * @param input 输入数据
     * @param datasourceId 数据源 ID
     * @return 创建的记录
     */
    public Map<String, Object> create(String typeName, Map<String, Object> input, Long datasourceId) {
        String tableName = NamingConverter.toSnakeCase(typeName);

        if (datasourceId == null) {
            throw new RuntimeException("无法确定数据源");
        }

        try {
            // 转换输入为下划线命名
            Map<String, Object> snakeCaseInput = convertToSnakeCase(input);

            // 构建 SQL
            String columns = String.join(", ", snakeCaseInput.keySet());
            String placeholders = snakeCaseInput.keySet().stream()
                .map(k -> "?")
                .collect(Collectors.joining(", "));

            String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";

            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            jdbcTemplate.update(sql, snakeCaseInput.values().toArray());

            // 返回刚创建的记录
            return getById(typeName, getGeneratedId(datasourceId, tableName), datasourceId);

        } catch (Exception e) {
            log.error("GraphQL 创建失败: table={}", tableName, e);
            throw new RuntimeException("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新记录
     * 
     * @param typeName 类型名称
     * @param id 记录 ID
     * @param input 输入数据
     * @param datasourceId 数据源 ID
     * @return 更新后的记录
     */
    public Map<String, Object> update(String typeName, String id, Map<String, Object> input, Long datasourceId) {
        String tableName = NamingConverter.toSnakeCase(typeName);

        if (datasourceId == null) {
            throw new RuntimeException("无法确定数据源");
        }

        try {
            TableMeta tableMeta = metadataService.getTableDetail(datasourceId, tableName);
            String primaryKey = tableMeta.getPrimaryKeys().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("表没有主键"));

            // 转换输入为下划线命名
            Map<String, Object> snakeCaseInput = convertToSnakeCase(input);

            // 构建 SET 子句
            String setClause = snakeCaseInput.keySet().stream()
                .map(k -> k + " = ?")
                .collect(Collectors.joining(", "));

            String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + primaryKey + " = ?";

            List<Object> params = new ArrayList<>(snakeCaseInput.values());
            params.add(id);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            int affected = jdbcTemplate.update(sql, params.toArray());

            if (affected > 0) {
                return getById(typeName, id, datasourceId);
            } else {
                throw new RuntimeException("记录不存在");
            }

        } catch (Exception e) {
            log.error("GraphQL 更新失败: table={}, id={}", tableName, id, e);
            throw new RuntimeException("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除记录
     * 
     * @param typeName 类型名称
     * @param id 记录 ID
     * @param datasourceId 数据源 ID
     * @return 是否删除成功
     */
    public boolean delete(String typeName, String id, Long datasourceId) {
        String tableName = NamingConverter.toSnakeCase(typeName);

        if (datasourceId == null) {
            throw new RuntimeException("无法确定数据源");
        }

        try {
            TableMeta tableMeta = metadataService.getTableDetail(datasourceId, tableName);
            String primaryKey = tableMeta.getPrimaryKeys().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("表没有主键"));

            String sql = "DELETE FROM " + tableName + " WHERE " + primaryKey + " = ?";

            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            int affected = jdbcTemplate.update(sql, id);

            return affected > 0;

        } catch (Exception e) {
            log.error("GraphQL 删除失败: table={}, id={}", tableName, id, e);
            throw new RuntimeException("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量创建
     * 
     * @param typeName 类型名称
     * @param inputs 输入数据列表
     * @param datasourceId 数据源 ID
     * @return 创建的记录列表
     */
    public List<Map<String, Object>> batchCreate(String typeName, List<Map<String, Object>> inputs, Long datasourceId) {
        if (inputs == null || inputs.isEmpty()) {
            throw new RuntimeException("输入数据不能为空");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> input : inputs) {
            results.add(create(typeName, input, datasourceId));
        }

        return results;
    }

    /**
     * 批量更新
     * 
     * @param typeName 类型名称
     * @param ids ID 列表
     * @param inputs 输入数据列表
     * @param datasourceId 数据源 ID
     * @return 更新后的记录列表
     */
    public List<Map<String, Object>> batchUpdate(String typeName, List<String> ids, 
            List<Map<String, Object>> inputs, Long datasourceId) {
        if (ids == null || inputs == null || ids.size() != inputs.size()) {
            throw new RuntimeException("ID 列表和输入数据列表必须一一对应");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            results.add(update(typeName, ids.get(i), inputs.get(i), datasourceId));
        }

        return results;
    }

    /**
     * 批量删除
     * 
     * @param typeName 类型名称
     * @param ids ID 列表
     * @param datasourceId 数据源 ID
     * @return 删除的记录数量
     */
    public int batchDelete(String typeName, List<String> ids, Long datasourceId) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("ID 列表不能为空");
        }

        String tableName = NamingConverter.toSnakeCase(typeName);

        if (datasourceId == null) {
            throw new RuntimeException("无法确定数据源");
        }

        try {
            TableMeta tableMeta = metadataService.getTableDetail(datasourceId, tableName);
            String primaryKey = tableMeta.getPrimaryKeys().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("表没有主键"));

            String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(", "));
            String sql = "DELETE FROM " + tableName + " WHERE " + primaryKey + " IN (" + placeholders + ")";

            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            return jdbcTemplate.update(sql, ids.toArray());

        } catch (Exception e) {
            log.error("GraphQL 批量删除失败: table={}", tableName, e);
            throw new RuntimeException("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * Upsert（存在则更新，不存在则创建）
     * 
     * @param typeName 类型名称
     * @param id 记录 ID
     * @param input 输入数据
     * @param datasourceId 数据源 ID
     * @return 操作后的记录
     */
    public Map<String, Object> upsert(String typeName, String id, Map<String, Object> input, Long datasourceId) {
        String tableName = NamingConverter.toSnakeCase(typeName);

        if (datasourceId == null) {
            throw new RuntimeException("无法确定数据源");
        }

        try {
            // 先尝试更新
            try {
                return update(typeName, id, input, datasourceId);
            } catch (RuntimeException e) {
                // 更新失败（记录不存在），则创建
                input.put("id", id);
                return create(typeName, input, datasourceId);
            }
        } catch (Exception e) {
            log.error("GraphQL upsert 失败: table={}, id={}", tableName, id, e);
            throw new RuntimeException("upsert 失败: " + e.getMessage());
        }
    }

    /**
     * 获取刚创建的记录 ID
     */
    private String getGeneratedId(Long datasourceId, String tableName) {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            // MySQL
            String sql = "SELECT LAST_INSERT_ID()";
            return jdbcTemplate.queryForObject(sql, String.class);

        } catch (Exception e) {
            log.warn("获取生成 ID 失败", e);
            return null;
        }
    }

    /**
     * 根据 ID 获取记录
     */
    private Map<String, Object> getById(String typeName, String id, Long datasourceId) {
        String tableName = NamingConverter.toSnakeCase(typeName);

        if (datasourceId == null) {
            throw new RuntimeException("无法确定数据源");
        }

        try {
            TableMeta tableMeta = metadataService.getTableDetail(datasourceId, tableName);
            String primaryKey = tableMeta.getPrimaryKeys().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("表没有主键"));

            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            String sql = "SELECT * FROM " + tableName + " WHERE " + primaryKey + " = ?";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, id);

            return convertToCamelCase(row);

        } catch (Exception e) {
            log.error("获取记录失败: table={}, id={}", tableName, id, e);
            throw new RuntimeException("获取记录失败: " + e.getMessage());
        }
    }

    /**
     * 将 Map 的键从下划线命名转换为驼峰命名
     */
    private Map<String, Object> convertToCamelCase(Map<String, Object> map) {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
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
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String snakeKey = NamingConverter.toSnakeCase(entry.getKey());
            result.put(snakeKey, entry.getValue());
        }
        return result;
    }
}
