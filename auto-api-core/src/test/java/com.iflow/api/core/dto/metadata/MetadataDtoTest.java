package com.iflow.api.core.dto.metadata;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 元数据 DTO 测试
 */
class MetadataDtoTest {

    @Test
    void testTableMeta() {
        TableMeta table = new TableMeta();
        table.setName("users");
        table.setRemarks("用户表");
        table.setType("TABLE");
        table.setCatalog("mydb");
        table.setSchema("public");

        assertEquals("users", table.getName());
        assertEquals("用户表", table.getRemarks());
        assertEquals("TABLE", table.getType());
        assertEquals("mydb", table.getCatalog());
        assertEquals("public", table.getSchema());
    }

    @Test
    void testColumnMeta() {
        ColumnMeta column = new ColumnMeta();
        column.setName("id");
        column.setTypeName("BIGINT");
        column.setColumnSize(20);
        column.setNullable(false);
        column.setAutoIncrement(true);
        column.setOrdinalPosition(1);

        assertEquals("id", column.getName());
        assertEquals("BIGINT", column.getTypeName());
        assertEquals(20, column.getColumnSize());
        assertFalse(column.isNullable());
        assertTrue(column.isAutoIncrement());
        assertEquals(1, column.getOrdinalPosition());
    }

    @Test
    void testIndexMeta() {
        IndexMeta index = new IndexMeta();
        index.setName("idx_user_name");
        index.setNonUnique(true);
        List<String> columns = new ArrayList<>();
        columns.add("user_name");
        columns.add("email");
        index.setColumns(columns);

        assertEquals("idx_user_name", index.getName());
        assertTrue(index.isNonUnique());
        assertEquals(2, index.getColumns().size());
        assertTrue(index.getColumns().contains("user_name"));
    }

    @Test
    void testForeignKeyMeta() {
        ForeignKeyMeta fk = new ForeignKeyMeta();
        fk.setFkName("fk_user_role");
        fk.setFkColumnName("role_id");
        fk.setPkTableName("roles");
        fk.setPkColumnName("id");
        fk.setDeleteRule("CASCADE");
        fk.setUpdateRule("NO ACTION");

        assertEquals("fk_user_role", fk.getFkName());
        assertEquals("role_id", fk.getFkColumnName());
        assertEquals("roles", fk.getPkTableName());
        assertEquals("id", fk.getPkColumnName());
        assertEquals("CASCADE", fk.getDeleteRule());
        assertEquals("NO ACTION", fk.getUpdateRule());
    }

    @Test
    void testTableMetaWithColumns() {
        TableMeta table = new TableMeta();
        List<ColumnMeta> columns = new ArrayList<>();

        ColumnMeta id = new ColumnMeta();
        id.setName("id");
        id.setTypeName("BIGINT");
        columns.add(id);

        ColumnMeta name = new ColumnMeta();
        name.setName("user_name");
        name.setTypeName("VARCHAR");
        columns.add(name);

        table.setName("users");
        table.setColumns(columns);

        assertEquals(2, table.getColumns().size());
        assertEquals("id", table.getColumns().get(0).getName());
        assertEquals("user_name", table.getColumns().get(1).getName());
    }

    @Test
    void testTableMetaWithPrimaryKeys() {
        TableMeta table = new TableMeta();
        table.setName("order_items");
        List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("order_id");
        primaryKeys.add("item_id");
        table.setPrimaryKeys(primaryKeys);

        assertEquals(2, table.getPrimaryKeys().size());
        assertTrue(table.getPrimaryKeys().contains("order_id"));
        assertTrue(table.getPrimaryKeys().contains("item_id"));
    }

    @Test
    void testTableMetaWithIndexes() {
        TableMeta table = new TableMeta();
        table.setName("products");

        List<IndexMeta> indexes = new ArrayList<>();
        IndexMeta idx1 = new IndexMeta();
        idx1.setName("idx_product_code");
        idx1.setNonUnique(false);
        List<String> idx1Cols = new ArrayList<>();
        idx1Cols.add("product_code");
        idx1.setColumns(idx1Cols);
        indexes.add(idx1);

        table.setIndexes(indexes);

        assertEquals(1, table.getIndexes().size());
        assertEquals("idx_product_code", table.getIndexes().get(0).getName());
    }

    @Test
    void testTableMetaWithForeignKeys() {
        TableMeta table = new TableMeta();
        table.setName("order_items");

        List<ForeignKeyMeta> foreignKeys = new ArrayList<>();
        ForeignKeyMeta fk = new ForeignKeyMeta();
        fk.setFkName("fk_order_user");
        fk.setFkColumnName("user_id");
        fk.setPkTableName("users");
        fk.setPkColumnName("id");
        foreignKeys.add(fk);

        table.setForeignKeys(foreignKeys);

        assertEquals(1, table.getForeignKeys().size());
        assertEquals("fk_order_user", table.getForeignKeys().get(0).getFkName());
    }

    @Test
    void testColumnMetaWithDefaultValue() {
        ColumnMeta column = new ColumnMeta();
        column.setName("status");
        column.setTypeName("VARCHAR");
        column.setColumnDef("'active'");
        column.setNullable(true);

        assertEquals("'active'", column.getColumnDef());
        assertTrue(column.isNullable());
    }
}
