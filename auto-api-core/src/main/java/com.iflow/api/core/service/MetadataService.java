package com.iflow.api.core.service;

import com.iflow.api.core.dto.metadata.ColumnMeta;
import com.iflow.api.core.dto.metadata.ForeignKeyMeta;
import com.iflow.api.core.dto.metadata.IndexMeta;
import com.iflow.api.core.dto.metadata.TableMeta;
import com.iflow.api.core.datasource.DynamicDataSourceService;
import com.iflow.api.core.entity.DatasourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

/**
 * 元数据读取服务
 * 
 * 引用：REQ-F4-003（扫描数据源获取所有表信息）
 *        REQ-F4-004（获取表字段信息、索引、外键）
 */
@Slf4j
@Service
public class MetadataService {

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    /**
     * 获取数据源中的所有表信息
     * 
     * @param datasourceId 数据源 ID
     * @return 表元数据列表
     */
    public List<TableMeta> getAllTables(Long datasourceId) {
        DatabaseMetaData metaData = dynamicDataSourceService.getDatabaseMetaData(datasourceId);
        List<TableMeta> tables = new ArrayList<>();

        try {
            String catalog = null;
            String schema = null;

            // 根据数据库类型获取 catalog 和 schema
            DatasourceConfig config = dynamicDataSourceService.getById(datasourceId)
                .orElseThrow(() -> new IllegalArgumentException("数据源不存在: " + datasourceId));

            switch (config.getType()) {
                case "mysql":
                    catalog = config.getDatabase();
                    break;
                case "postgresql":
                    schema = "public";
                    break;
                case "oracle":
                    schema = config.getUsername().toUpperCase();
                    break;
            }

            // 获取表信息
            try (ResultSet rs = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    TableMeta table = extractTableInfo(rs, config.getType());
                    tables.add(table);
                }
            }

            log.info("获取到 {} 个表信息: datasourceId={}", tables.size(), datasourceId);
            return tables;

        } catch (SQLException e) {
            log.error("获取表信息失败: datasourceId={}", datasourceId, e);
            throw new RuntimeException("获取表信息失败", e);
        }
    }

    /**
     * 获取指定表的详细信息
     * 
     * @param datasourceId 数据源 ID
     * @param tableName 表名
     * @return 表元数据
     */
    public TableMeta getTableDetail(Long datasourceId, String tableName) {
        DatabaseMetaData metaData = dynamicDataSourceService.getDatabaseMetaData(datasourceId);
        DatasourceConfig config = dynamicDataSourceService.getById(datasourceId)
            .orElseThrow(() -> new IllegalArgumentException("数据源不存在: " + datasourceId));

        try {
            String catalog = null;
            String schema = null;

            switch (config.getType()) {
                case "mysql":
                    catalog = config.getDatabase();
                    break;
                case "postgresql":
                    schema = "public";
                    break;
                case "oracle":
                    schema = config.getUsername().toUpperCase();
                    break;
            }

            // 获取表基本信息
            TableMeta table = null;
            try (ResultSet rs = metaData.getTables(catalog, schema, tableName, new String[]{"TABLE"})) {
                if (rs.next()) {
                    table = extractTableInfo(rs, config.getType());
                }
            }

            if (table == null) {
                throw new IllegalArgumentException("表不存在: " + tableName);
            }

            // 获取字段信息
            table.setColumns(getColumns(datasourceId, tableName, catalog, schema));

            // 获取主键信息
            table.setPrimaryKeys(getPrimaryKeys(metaData, catalog, schema, tableName));

            // 获取索引信息
            table.setIndexes(getIndexes(metaData, catalog, schema, tableName));

            // 获取外键信息
            table.setForeignKeys(getForeignKeys(metaData, catalog, schema, tableName));

            return table;

        } catch (SQLException e) {
            log.error("获取表详情失败: datasourceId={}, tableName={}", datasourceId, tableName, e);
            throw new RuntimeException("获取表详情失败", e);
        }
    }

    /**
     * 获取表的字段信息
     */
    private List<ColumnMeta> getColumns(Long datasourceId, String tableName, 
            String catalog, String schema) throws SQLException {
        DatabaseMetaData metaData = dynamicDataSourceService.getDatabaseMetaData(datasourceId);
        List<ColumnMeta> columns = new ArrayList<>();

        try (ResultSet rs = metaData.getColumns(catalog, schema, tableName, "%")) {
            while (rs.next()) {
                ColumnMeta column = new ColumnMeta();
                column.setName(rs.getString("COLUMN_NAME"));
                column.setTypeName(rs.getString("TYPE_NAME"));
                column.setColumnSize(rs.getInt("COLUMN_SIZE"));
                column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                column.setColumnDef(rs.getString("COLUMN_DEF"));
                column.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));
                column.setRemarks(rs.getString("REMARKS"));
                
                // 判断是否为自增字段
                column.setAutoIncrement("YES".equals(rs.getString("IS_AUTOINCREMENT")));
                
                columns.add(column);
            }
        }

        return columns;
    }

    /**
     * 获取主键信息
     */
    private List<String> getPrimaryKeys(DatabaseMetaData metaData, String catalog, 
            String schema, String tableName) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();

        try (ResultSet rs = metaData.getPrimaryKeys(catalog, schema, tableName)) {
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        }

        return primaryKeys;
    }

    /**
     * 获取索引信息
     */
    private List<IndexMeta> getIndexes(DatabaseMetaData metaData, String catalog, 
            String schema, String tableName) throws SQLException {
        List<IndexMeta> indexes = new ArrayList<>();

        try (ResultSet rs = metaData.getIndexInfo(catalog, schema, tableName, false, true)) {
            IndexMeta currentIndex = null;
            String lastIndexName = null;

            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                boolean nonUnique = rs.getBoolean("NON_UNIQUE");
                String columnName = rs.getString("COLUMN_NAME");

                if (indexName == null) {
                    continue;
                }

                // 新索引
                if (!indexName.equals(lastIndexName)) {
                    if (currentIndex != null) {
                        indexes.add(currentIndex);
                    }
                    currentIndex = new IndexMeta();
                    currentIndex.setName(indexName);
                    currentIndex.setNonUnique(nonUnique);
                    currentIndex.setColumns(new ArrayList<>());
                    lastIndexName = indexName;
                }

                if (currentIndex != null && columnName != null) {
                    // 创建 IndexColumn 对象
                    IndexMeta.IndexColumn indexColumn = new IndexMeta.IndexColumn();
                    indexColumn.setColumnName(columnName);
                    currentIndex.getColumns().add(indexColumn);
                }
            }

            // 添加最后一个索引
            if (currentIndex != null) {
                indexes.add(currentIndex);
            }
        }

        return indexes;
    }

    /**
     * 获取外键信息
     */
    private List<ForeignKeyMeta> getForeignKeys(DatabaseMetaData metaData, String catalog, 
            String schema, String tableName) throws SQLException {
        List<ForeignKeyMeta> foreignKeys = new ArrayList<>();

        try (ResultSet rs = metaData.getImportedKeys(catalog, schema, tableName)) {
            while (rs.next()) {
                ForeignKeyMeta fk = new ForeignKeyMeta();
                fk.setFkName(rs.getString("FK_NAME"));
                fk.setFkColumnName(rs.getString("FKCOLUMN_NAME"));
                fk.setPkTableName(rs.getString("PKTABLE_NAME"));
                fk.setPkColumnName(rs.getString("PKCOLUMN_NAME"));
                fk.setDeleteRule(convertDeleteRule(rs.getShort("DELETE_RULE")));
                fk.setUpdateRule(convertUpdateRule(rs.getShort("UPDATE_RULE")));
                foreignKeys.add(fk);
            }
        }

        return foreignKeys;
    }

    /**
     * 提取表基本信息
     */
    private TableMeta extractTableInfo(ResultSet rs, String dbType) throws SQLException {
        TableMeta table = new TableMeta();
        table.setName(rs.getString("TABLE_NAME"));
        table.setType(rs.getString("TABLE_TYPE"));
        table.setRemarks(rs.getString("REMARKS"));
        
        // 根据数据库类型设置 catalog 和 schema
        switch (dbType) {
            case "mysql":
                table.setCatalog(rs.getString("TABLE_CAT"));
                break;
            case "postgresql":
                table.setSchema(rs.getString("TABLE_SCHEM"));
                break;
            case "oracle":
                table.setSchema(rs.getString("TABLE_SCHEM"));
                break;
        }
        
        return table;
    }

    /**
     * 转换删除规则
     */
    private String convertDeleteRule(short rule) {
        switch (rule) {
            case DatabaseMetaData.importedKeyCascade:
                return "CASCADE";
            case DatabaseMetaData.importedKeySetNull:
                return "SET NULL";
            case DatabaseMetaData.importedKeySetDefault:
                return "SET DEFAULT";
            case DatabaseMetaData.importedKeyRestrict:
                return "RESTRICT";
            default:
                return "NO ACTION";
        }
    }

    /**
     * 转换更新规则
     */
    private String convertUpdateRule(short rule) {
        switch (rule) {
            case DatabaseMetaData.importedKeyCascade:
                return "CASCADE";
            case DatabaseMetaData.importedKeySetNull:
                return "SET NULL";
            case DatabaseMetaData.importedKeySetDefault:
                return "SET DEFAULT";
            case DatabaseMetaData.importedKeyRestrict:
                return "RESTRICT";
            default:
                return "NO ACTION";
        }
    }

    /**
     * 获取数据库类型
     */
    public String getDatabaseType(Long datasourceId) {
        return dynamicDataSourceService.getById(datasourceId)
            .map(DatasourceConfig::getType)
            .orElse("unknown");
    }

    /**
     * 获取数据库版本信息
     */
    public String getDatabaseVersion(Long datasourceId) {
        try {
            DatabaseMetaData metaData = dynamicDataSourceService.getDatabaseMetaData(datasourceId);
            return metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion();
        } catch (SQLException e) {
            log.error("获取数据库版本失败: datasourceId={}", datasourceId, e);
            return "Unknown";
        }
    }
}
