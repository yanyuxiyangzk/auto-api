package com.iflow.api.core.graphql;

import com.iflow.api.core.datasource.DynamicDataSourceService;
import com.iflow.api.core.dto.metadata.TableMeta;
import com.iflow.api.core.service.MetadataService;
import com.iflow.api.core.util.NamingConverter;
import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GraphQL Query Resolver
 * 
 * 引用：REQ-F1-004（自动生成 Query API）
 */
@Slf4j
@Component
public class GraphQLQueryResolver {

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private GraphQLSchemaGenerator schemaGenerator;

    /**
     * 获取列表查询
     * 
     * @param env GraphQL 环境
     * @return 数据列表
     */
    public List<Map<String, Object>> queryList(DataFetchingEnvironment env) {
        String typeName = getTypeName(env);
        String tableName = NamingConverter.toSnakeCase(typeName);
        Long datasourceId = getDatasourceId(env);

        if (datasourceId == null) {
            throw new RuntimeException("无法确定数据源");
        }

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            String sql = "SELECT * FROM " + tableName + " LIMIT 100";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

            // 转换字段名
            return rows.stream()
                .map(this::convertToCamelCase)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("GraphQL 查询列表失败: table={}", tableName, e);
            throw new RuntimeException("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取单条记录
     * 
     * @param env GraphQL 环境
     * @return 单条记录
     */
    public Map<String, Object> queryById(DataFetchingEnvironment env) {
        String typeName = getTypeName(env);
        String tableName = NamingConverter.toSnakeCase(typeName);
        Long datasourceId = getDatasourceId(env);
        String id = env.getArgument("id");

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
            log.error("GraphQL 查询详情失败: table={}, id={}", tableName, id, e);
            throw new RuntimeException("查询失败: " + e.getMessage());
        }
    }

    /**
     * 条件查询
     * 
     * @param env GraphQL 环境
     * @return 符合条件的数据列表
     */
    public List<Map<String, Object>> queryWithCondition(DataFetchingEnvironment env) {
        String typeName = getTypeName(env);
        String tableName = NamingConverter.toSnakeCase(typeName);
        Long datasourceId = getDatasourceId(env);

        if (datasourceId == null) {
            throw new RuntimeException("无法确定数据源");
        }

        Map<String, Object> args = env.getArguments();
        args.remove("id"); // 移除 id 参数

        if (args.isEmpty()) {
            return queryList(env);
        }

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " WHERE ");
            List<Object> params = new ArrayList<>();

            for (Map.Entry<String, Object> entry : args.entrySet()) {
                String column = NamingConverter.toSnakeCase(entry.getKey());
                if (sql.length() > 30) {
                    sql.append(" AND ");
                }
                sql.append(column).append(" = ?");
                params.add(entry.getValue());
            }

            sql.append(" LIMIT 100");

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            return rows.stream()
                .map(this::convertToCamelCase)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("GraphQL 条件查询失败: table={}", tableName, e);
            throw new RuntimeException("查询失败: " + e.getMessage());
        }
    }

    /**
     * 关联查询（支持外键关联）
     * 
     * @param env GraphQL 环境
     * @return 关联数据
     */
    public List<Map<String, Object>> queryWithRelation(DataFetchingEnvironment env) {
        String typeName = getTypeName(env);
        String tableName = NamingConverter.toSnakeCase(typeName);
        Long datasourceId = getDatasourceId(env);

        if (datasourceId == null) {
            throw new RuntimeException("无法确定数据源");
        }

        try {
            TableMeta tableMeta = metadataService.getTableDetail(datasourceId, tableName);

            // 获取关联数据（外键关联）
            List<Map<String, Object>> result = new ArrayList<>();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            String sql = "SELECT * FROM " + tableName + " LIMIT 100";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

            for (Map<String, Object> row : rows) {
                Map<String, Object> enrichedRow = new LinkedHashMap<>(convertToCamelCase(row));

                // 添加关联数据
                for (com.iflow.api.core.dto.metadata.ForeignKeyMeta fk : tableMeta.getForeignKeys()) {
                    String relationFieldName = NamingConverter.toCamelCase(fk.getFkName());
                    Object relatedData = fetchRelatedData(datasourceId, fk, row);
                    enrichedRow.put(relationFieldName, relatedData);
                }

                result.add(enrichedRow);
            }

            return result;

        } catch (Exception e) {
            log.error("GraphQL 关联查询失败: table={}", tableName, e);
            throw new RuntimeException("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取关联数据
     */
    private Object fetchRelatedData(Long datasourceId, 
            com.iflow.api.core.dto.metadata.ForeignKeyMeta fk, 
            Map<String, Object> row) {
        try {
            String fkColumn = NamingConverter.toSnakeCase(fk.getFkColumnName());
            Object fkValue = row.get(fkColumn);

            if (fkValue == null) {
                return null;
            }

            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            String pkTable = fk.getPkTableName();
            String pkColumn = NamingConverter.toSnakeCase(fk.getPkColumnName());

            String sql = "SELECT * FROM " + pkTable + " WHERE " + pkColumn + " = ?";
            Map<String, Object> relatedRow = jdbcTemplate.queryForMap(sql, fkValue);

            return convertToCamelCase(relatedRow);

        } catch (Exception e) {
            log.warn("获取关联数据失败: fk={}", fk.getFkName(), e);
            return null;
        }
    }

    /**
     * 统计查询
     * 
     * @param env GraphQL 环境
     * @return 统计数据
     */
    public Map<String, Object> queryCount(DataFetchingEnvironment env) {
        String typeName = getTypeName(env);
        String tableName = NamingConverter.toSnakeCase(typeName);
        Long datasourceId = getDatasourceId(env);

        if (datasourceId == null) {
            throw new RuntimeException("无法确定数据源");
        }

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            String sql = "SELECT COUNT(*) as total FROM " + tableName;
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);

            Map<String, Object> countResult = new HashMap<>();
            countResult.put("total", result.get("total"));
            countResult.put("type", typeName);

            return countResult;

        } catch (Exception e) {
            log.error("GraphQL 统计查询失败: table={}", tableName, e);
            throw new RuntimeException("查询失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询
     * 
     * @param env GraphQL 环境
     * @return 分页数据
     */
    public Map<String, Object> queryPaginated(DataFetchingEnvironment env) {
        String typeName = getTypeName(env);
        String tableName = NamingConverter.toSnakeCase(typeName);
        Long datasourceId = getDatasourceId(env);

        if (datasourceId == null) {
            throw new RuntimeException("无法确定数据源");
        }

        // 获取分页参数
        Integer page = env.getArgument("page");
        Integer size = env.getArgument("size");
        String orderBy = env.getArgument("orderBy");
        String orderDir = env.getArgument("orderDir");

        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 10;
        }
        if (orderBy == null) {
            orderBy = "id";
        }
        if (orderDir == null) {
            orderDir = "DESC";
        }

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                dynamicDataSourceService.getOrCreateDataSource(datasourceId)
            );

            // 查询总数
            String countSql = "SELECT COUNT(*) FROM " + tableName;
            int total = jdbcTemplate.queryForObject(countSql, Integer.class);

            // 查询数据
            String orderColumn = NamingConverter.toSnakeCase(orderBy);
            String dataSql = "SELECT * FROM " + tableName 
                + " ORDER BY " + orderColumn + " " + orderDir
                + " LIMIT " + size + " OFFSET " + ((page - 1) * size);

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(dataSql);

            Map<String, Object> result = new HashMap<>();
            result.put("items", rows.stream().map(this::convertToCamelCase).collect(Collectors.toList()));
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (total + size - 1) / size);

            return result;

        } catch (Exception e) {
            log.error("GraphQL 分页查询失败: table={}", tableName, e);
            throw new RuntimeException("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取类型名称
     */
    private String getTypeName(DataFetchingEnvironment env) {
        graphql.schema.GraphQLType parentType = env.getParentType();
        if (parentType instanceof graphql.schema.GraphQLNamedOutputType) {
            return ((graphql.schema.GraphQLNamedOutputType) parentType).getName();
        }
        return "Unknown";
    }

    /**
     * 获取数据源 ID
     */
    private Long getDatasourceId(DataFetchingEnvironment env) {
        // 从上下文或参数中获取
        Object datasourceId = env.getContext();
        if (datasourceId instanceof Long) {
            return (Long) datasourceId;
        }
        return 1L; // 默认数据源
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
}
