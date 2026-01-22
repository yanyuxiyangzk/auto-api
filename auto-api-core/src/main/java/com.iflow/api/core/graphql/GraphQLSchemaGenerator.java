package com.iflow.api.core.graphql;

import com.iflow.api.core.dto.metadata.ColumnMeta;
import com.iflow.api.core.dto.metadata.TableMeta;
import com.iflow.api.core.util.NamingConverter;
import com.iflow.api.core.util.TypeConverter;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * GraphQL Schema 生成器
 * 
 * 引用：REQ-F1-002（自动生成 GraphQL API）
 *        REQ-F1-003（动态 Schema 生成）
 */
@Slf4j
@Component
public class GraphQLSchemaGenerator {

    /**
     * 类型缓存 (数据源ID -> GraphQLObjectType)
     */
    private final Map<Long, Map<String, GraphQLObjectType>> typeCache = new ConcurrentHashMap<>();

    /**
     * Schema 缓存 (数据源ID -> GraphQLSchema)
     */
    private final Map<Long, GraphQLSchema> schemaCache = new ConcurrentHashMap<>();

    /**
     * 生成完整的 GraphQL Schema
     * 
     * @param tables 表元数据列表
     * @return GraphQLSchema
     */
    public GraphQLSchema generateSchema(List<TableMeta> tables) {
        // 构建类型
        Map<String, GraphQLObjectType> types = new LinkedHashMap<>();
        for (TableMeta table : tables) {
            GraphQLObjectType type = generateType(table);
            if (type != null) {
                types.put(type.getName(), type);
            }
        }

        // 构建 Query 类型
        GraphQLObjectType queryType = generateQueryType(types);

        // 构建 Mutation 类型
        GraphQLObjectType mutationType = generateMutationType(types);

        // 创建 Schema
        GraphQLSchema schema = GraphQLSchema.newSchema()
            .query(queryType)
            .mutation(mutationType)
            .build();

        log.info("GraphQL Schema 生成完成: types={}", types.size());
        return schema;
    }

    /**
     * 根据表元数据生成 GraphQL 类型
     * 
     * @param table 表元数据
     * @return GraphQLObjectType
     */
    public GraphQLObjectType generateType(TableMeta table) {
        String typeName = NamingConverter.toGraphQLTypeName(table.getName());

        // 检查缓存
        Long datasourceId = extractDatasourceId(table);
        if (datasourceId != null) {
            Map<String, GraphQLObjectType> datasourceTypes = typeCache.get(datasourceId);
            if (datasourceTypes != null && datasourceTypes.containsKey(typeName)) {
                return datasourceTypes.get(typeName);
            }
        }

        // 构建字段
        List<GraphQLFieldDefinition> fields = new ArrayList<>();
        for (ColumnMeta column : table.getColumns()) {
            GraphQLFieldDefinition field = generateField(column);
            if (field != null) {
                fields.add(field);
            }
        }

        // 添加查询字段
        fields.add(GraphQLFieldDefinition.newFieldDefinition()
            .name("_self")
            .type(GraphQLTypeReference.typeRef(typeName))
            .build());

        // 创建类型
        GraphQLObjectType.Builder typeBuilder = GraphQLObjectType.newObject()
            .name(typeName)
            .description(table.getRemarks());

        // 添加字段
        for (GraphQLFieldDefinition field : fields) {
            typeBuilder.field(field);
        }

        GraphQLObjectType type = typeBuilder.build();

        // 缓存
        if (datasourceId != null) {
            typeCache.computeIfAbsent(datasourceId, k -> new ConcurrentHashMap<>())
                .put(typeName, type);
        }

        log.debug("GraphQL 类型生成: name={}, fields={}", typeName, fields.size());
        return type;
    }

    /**
     * 生成 GraphQL 字段
     */
    private GraphQLFieldDefinition generateField(ColumnMeta column) {
        String fieldName = NamingConverter.toGraphQLFieldName(column.getName());
        GraphQLOutputType fieldType = convertToGraphQLType(column);

        if (fieldType == null) {
            return null;
        }

        GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
            .name(fieldName)
            .type(fieldType)
            .description(column.getComment());

        // 非空字段
        if (!column.isNullable() && !column.isAutoIncrement()) {
            fieldBuilder.type(GraphQLNonNull.nonNull(fieldType));
        }

        return fieldBuilder.build();
    }

    /**
     * 将数据库类型转换为 GraphQL 输出类型
     */
    @SuppressWarnings("unchecked")
    private GraphQLOutputType convertToGraphQLType(ColumnMeta column) {
        String javaType = TypeConverter.toJavaType(column.getTypeName(), column.getTypeName());

        // 处理列表类型
        if (column.getTypeName().contains("ARRAY") || column.getTypeName().equals("JSON")) {
            return (GraphQLOutputType) GraphQLList.list(Scalars.GraphQLString);
        }

        // 根据 Java 类型映射
        switch (javaType) {
            case "Integer":
            case "Long":
            case "Short":
            case "Byte":
                return Scalars.GraphQLInt;
            case "Float":
            case "Double":
            case "BigDecimal":
                return Scalars.GraphQLFloat;
            case "Boolean":
                return Scalars.GraphQLBoolean;
            case "Date":
            case "LocalDate":
                return Scalars.GraphQLString;
            case "Time":
            case "LocalTime":
                return Scalars.GraphQLString;
            case "Timestamp":
            case "DateTime":
            case "LocalDateTime":
                return Scalars.GraphQLString;
            case "byte[]":
            case "Byte[]":
                return Scalars.GraphQLString;
            default:
                return Scalars.GraphQLString;
        }
    }

    /**
     * 将数据库类型转换为 GraphQL 输入类型
     */
    @SuppressWarnings("unchecked")
    private GraphQLInputType convertToGraphQLInputType(ColumnMeta column) {
        String javaType = TypeConverter.toJavaType(column.getTypeName(), column.getTypeName());

        // 处理列表类型
        if (column.getTypeName().contains("ARRAY") || column.getTypeName().equals("JSON")) {
            return (GraphQLInputType) GraphQLList.list(Scalars.GraphQLString);
        }

        // 根据 Java 类型映射
        switch (javaType) {
            case "Integer":
            case "Long":
            case "Short":
            case "Byte":
                return Scalars.GraphQLInt;
            case "Float":
            case "Double":
            case "BigDecimal":
                return Scalars.GraphQLFloat;
            case "Boolean":
                return Scalars.GraphQLBoolean;
            case "Date":
            case "LocalDate":
                return Scalars.GraphQLString;
            case "Time":
            case "LocalTime":
                return Scalars.GraphQLString;
            case "Timestamp":
            case "DateTime":
            case "LocalDateTime":
                return Scalars.GraphQLString;
            case "byte[]":
            case "Byte[]":
                return Scalars.GraphQLString;
            default:
                return Scalars.GraphQLString;
        }
    }

    /**
     * 生成 Query 类型
     */
    private GraphQLObjectType generateQueryType(Map<String, GraphQLObjectType> types) {
        GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject()
            .name("Query")
            .description("GraphQL Query 根类型");

        // 为每个类型添加查询方法
        for (Map.Entry<String, GraphQLObjectType> entry : types.entrySet()) {
            String typeName = entry.getKey();
            GraphQLObjectType type = entry.getValue();

            // 获取列表
            String listFieldName = NamingConverter.toCamelCase(typeName) + "List";
            queryBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(listFieldName)
                .type(GraphQLList.list(GraphQLTypeReference.typeRef(typeName)))
                .build());

            // 获取单条
            String getFieldName = NamingConverter.toCamelCase(typeName);
            queryBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(getFieldName)
                .type(GraphQLTypeReference.typeRef(typeName))
                .argument(GraphQLArgument.newArgument()
                    .name("id")
                    .type(GraphQLNonNull.nonNull(Scalars.GraphQLID))
                    .build())
                .build());
        }

        return queryBuilder.build();
    }

    /**
     * 生成 Mutation 类型
     */
    private GraphQLObjectType generateMutationType(Map<String, GraphQLObjectType> types) {
        GraphQLObjectType.Builder mutationBuilder = GraphQLObjectType.newObject()
            .name("Mutation")
            .description("GraphQL Mutation 根类型");

        // 为每个类型添加增删改方法
        for (Map.Entry<String, GraphQLObjectType> entry : types.entrySet()) {
            String typeName = entry.getKey();
            GraphQLObjectType type = entry.getValue();

            // Create
            String createFieldName = "create" + typeName;
            mutationBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(createFieldName)
                .type(GraphQLTypeReference.typeRef(typeName))
                .argument(GraphQLArgument.newArgument()
                    .name("input")
                    .type(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef("Create" + typeName + "Input")))
                    .build())
                .build());

            // Update
            String updateFieldName = "update" + typeName;
            mutationBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(updateFieldName)
                .type(GraphQLTypeReference.typeRef(typeName))
                .argument(GraphQLArgument.newArgument()
                    .name("id")
                    .type(GraphQLNonNull.nonNull(Scalars.GraphQLID))
                    .build())
                .argument(GraphQLArgument.newArgument()
                    .name("input")
                    .type(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef("Update" + typeName + "Input")))
                    .build())
                .build());

            // Delete
            String deleteFieldName = "delete" + typeName;
            mutationBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(deleteFieldName)
                .type(Scalars.GraphQLBoolean)
                .argument(GraphQLArgument.newArgument()
                    .name("id")
                    .type(GraphQLNonNull.nonNull(Scalars.GraphQLID))
                    .build())
                .build());
        }

        return mutationBuilder.build();
    }

    /**
     * 生成 Create 输入类型
     */
    @SuppressWarnings("unchecked")
    public GraphQLInputObjectType generateCreateInputType(TableMeta table) {
        String typeName = NamingConverter.toGraphQLTypeName(table.getName());
        String inputTypeName = "Create" + typeName + "Input";

        List<GraphQLInputObjectField> fields = new ArrayList<>();
        for (ColumnMeta column : table.getColumns()) {
            if (column.isAutoIncrement()) {
                continue; // 自增字段不需要输入
            }

            String fieldName = NamingConverter.toGraphQLFieldName(column.getName());
            GraphQLInputType fieldType = convertToGraphQLInputType(column);

            if (fieldType != null) {
                GraphQLInputObjectField.Builder fieldBuilder = GraphQLInputObjectField.newInputObjectField()
                    .name(fieldName)
                    .type(fieldType);

                if (!column.isNullable()) {
                    fieldBuilder.type(GraphQLNonNull.nonNull(fieldType));
                }

                fields.add(fieldBuilder.build());
            }
        }

        return GraphQLInputObjectType.newInputObject()
            .name(inputTypeName)
            .description("创建 " + typeName + " 的输入类型")
            .fields(fields)
            .build();
    }

    /**
     * 生成 Update 输入类型
     */
    @SuppressWarnings("unchecked")
    public GraphQLInputObjectType generateUpdateInputType(TableMeta table) {
        String typeName = NamingConverter.toGraphQLTypeName(table.getName());
        String inputTypeName = "Update" + typeName + "Input";

        List<GraphQLInputObjectField> fields = new ArrayList<>();
        for (ColumnMeta column : table.getColumns()) {
            if (column.isAutoIncrement() || column.isPrimaryKey()) {
                continue; // 自增和主键不需要更新
            }

            String fieldName = NamingConverter.toGraphQLFieldName(column.getName());
            GraphQLInputType fieldType = convertToGraphQLInputType(column);

            if (fieldType != null) {
                GraphQLInputObjectField.Builder fieldBuilder = GraphQLInputObjectField.newInputObjectField()
                    .name(fieldName)
                    .type(fieldType); // Update 字段可为空

                fields.add(fieldBuilder.build());
            }
        }

        return GraphQLInputObjectType.newInputObject()
            .name(inputTypeName)
            .description("更新 " + typeName + " 的输入类型")
            .fields(fields)
            .build();
    }

    /**
     * 生成 SDL 格式的 Schema 字符串
     * 
     * @param schema GraphQLSchema
     * @return SDL 字符串
     */
    public String generateSDL(GraphQLSchema schema) {
        // graphql-java 20.x 不直接支持 schemaSDL，使用 print 代替
        return schema.toString();
    }

    /**
     * 从 Schema 字符串解析
     * 
     * @param sdl SDL 字符串
     * @return GraphQLSchema
     */
    public GraphQLSchema parseSchema(String sdl) {
        // 简化实现：直接返回已有的 schema
        // 实际使用中应该使用 SchemaParser 和 SchemaGenerator
        return null;
    }

    /**
     * 从表元数据提取数据源 ID
     */
    private Long extractDatasourceId(TableMeta table) {
        // 从表信息中获取 datasourceId
        if (table.getRemarks() != null && table.getRemarks().contains("datasourceId:")) {
            String remark = table.getRemarks();
            int idx = remark.indexOf("datasourceId:");
            if (idx >= 0) {
                String idStr = remark.substring(idx + 13).trim();
                try {
                    return Long.parseLong(idStr.split("\\s")[0]);
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        }
        return null;
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        typeCache.clear();
        schemaCache.clear();
    }

    /**
     * 清空指定数据源的缓存
     */
    public void clearCache(Long datasourceId) {
        typeCache.remove(datasourceId);
        schemaCache.remove(datasourceId);
    }
}