package com.iflow.api.core.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iflow.api.core.entity.TableSelection;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 表选择配置 Repository
 * 
 * 引用：REQ-F0-003（黑白名单机制）
 */
@Mapper
public interface TableSelectionRepository extends BaseMapper<TableSelection> {

    /**
     * 根据数据源 ID 和是否选择查询
     */
    @Select("SELECT * FROM api_table_selection WHERE datasource_id = #{datasourceId} AND selected = #{selected} AND deleted = 0 ORDER BY table_name ASC")
    List<TableSelection> findByDatasourceIdAndSelected(
            @Param("datasourceId") Long datasourceId, 
            @Param("selected") Boolean selected);
    @Select("SELECT * FROM api_table_selection WHERE datasource_id = #{datasourceId} AND deleted = 0 ORDER BY priority DESC, id ASC")
    List<TableSelection> findByDatasourceId(@Param("datasourceId") Long datasourceId);

    /**
     * 根据数据源 ID 和表名查询
     */
    @Select("SELECT * FROM api_table_selection WHERE datasource_id = #{datasourceId} AND table_name = #{tableName} AND deleted = 0")
    Optional<TableSelection> findByDatasourceIdAndTableName(
            @Param("datasourceId") Long datasourceId, 
            @Param("tableName") String tableName);

    /**
     * 查询所有已选择的表
     */
    @Select("SELECT * FROM api_table_selection WHERE selected = 1 AND deleted = 0 ORDER BY datasource_id, priority DESC")
    List<TableSelection> findAllSelected();

    /**
     * 根据数据源 ID 删除所有选择配置
     */
    @Delete("DELETE FROM api_table_selection WHERE datasource_id = #{datasourceId}")
    int deleteByDatasourceId(@Param("datasourceId") Long datasourceId);

    /**
     * 批量更新选择状态
     */
    @Delete("UPDATE api_table_selection SET selected = #{selected}, updated_at = NOW() WHERE datasource_id = #{datasourceId}")
    int updateSelectedByDatasourceId(
            @Param("datasourceId") Long datasourceId, 
            @Param("selected") Boolean selected);

    /**
     * 根据表名查询
     */
    @Select("SELECT * FROM api_table_selection WHERE table_name = #{tableName} AND deleted = 0")
    List<TableSelection> findByTableName(@Param("tableName") String tableName);

    /**
     * 检查表是否被选择
     */
    @Select("SELECT COUNT(*) FROM api_table_selection WHERE datasource_id = #{datasourceId} AND table_name = #{tableName} AND selected = 1 AND deleted = 0")
    int countSelectedByDatasourceIdAndTableName(
            @Param("datasourceId") Long datasourceId, 
            @Param("tableName") String tableName);

    /**
     * 获取已选择表的数量
     */
    @Select("SELECT COUNT(*) FROM api_table_selection WHERE datasource_id = #{datasourceId} AND selected = 1 AND deleted = 0")
    int countSelectedByDatasourceId(@Param("datasourceId") Long datasourceId);

    /**
     * 获取数据源下所有表的数量
     */
    @Select("SELECT COUNT(*) FROM api_table_selection WHERE datasource_id = #{datasourceId} AND deleted = 0")
    int countByDatasourceId(@Param("datasourceId") Long datasourceId);
}
