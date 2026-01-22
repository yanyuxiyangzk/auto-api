package com.iflow.api.core.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iflow.api.core.entity.ApiGenerationStatus;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

/**
 * API 生成状态 Repository
 * 
 * 引用：REQ-F0-010（刷新 API 配置接口）
 */
@Mapper
public interface ApiGenerationStatusRepository extends BaseMapper<ApiGenerationStatus> {

    /**
     * 根据数据源 ID 查询所有状态
     */
    @Select("SELECT * FROM api_generation_status WHERE datasource_id = #{datasourceId} ORDER BY table_name ASC")
    List<ApiGenerationStatus> findByDatasourceId(@Param("datasourceId") Long datasourceId);

    /**
     * 根据表选择 ID 查询
     */
    @Select("SELECT * FROM api_generation_status WHERE table_selection_id = #{tableSelectionId}")
    Optional<ApiGenerationStatus> findByTableSelectionId(@Param("tableSelectionId") Long tableSelectionId);

    /**
     * 根据数据源 ID 和状态查询所有
     */
    @Select("SELECT * FROM api_generation_status WHERE datasource_id = #{datasourceId} AND status = #{status} ORDER BY table_name ASC")
    List<ApiGenerationStatus> findByDatasourceIdAndStatus(
            @Param("datasourceId") Long datasourceId, 
            @Param("status") String status);

    /**
     * 查询所有活跃的 API
     */
    @Select("SELECT * FROM api_generation_status WHERE status = 'ACTIVE' ORDER BY datasource_id, table_name ASC")
    List<ApiGenerationStatus> findAllActive();
    @Select("SELECT * FROM api_generation_status WHERE table_name = #{tableName} ORDER BY datasource_id ASC")
    List<ApiGenerationStatus> findByTableName(@Param("tableName") String tableName);

    /**
     * 根据数据源 ID 和表名查询
     */
    @Select("SELECT * FROM api_generation_status WHERE datasource_id = #{datasourceId} AND table_name = #{tableName}")
    Optional<ApiGenerationStatus> findByDatasourceIdAndTableName(
            @Param("datasourceId") Long datasourceId, 
            @Param("tableName") String tableName);

    /**
     * 查询所有已生成的 API
     */
    @Select("SELECT * FROM api_generation_status WHERE status = 'generated' ORDER BY datasource_id, table_name ASC")
    List<ApiGenerationStatus> findAllGenerated();

    /**
     * 查询所有待生成的 API
     */
    @Select("SELECT * FROM api_generation_status WHERE status = 'pending' OR status = 'generating' ORDER BY datasource_id, table_name ASC")
    List<ApiGenerationStatus> findPendingOrGenerating();

    /**
     * 查询所有错误状态的 API
     */
    @Select("SELECT * FROM api_generation_status WHERE status = 'error' ORDER BY datasource_id, table_name ASC")
    List<ApiGenerationStatus> findAllError();

    /**
     * 根据数据源 ID 删除所有状态
     */
    @Delete("DELETE FROM api_generation_status WHERE datasource_id = #{datasourceId}")
    int deleteByDatasourceId(@Param("datasourceId") Long datasourceId);

    /**
     * 根据状态查询
     */
    @Select("SELECT * FROM api_generation_status WHERE status = #{status} ORDER BY datasource_id, table_name ASC")
    List<ApiGenerationStatus> findByStatus(@Param("status") String status);

    /**
     * 更新 REST API 注册状态
     */
    @Update("UPDATE api_generation_status SET rest_api_registered = #{registered}, status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateRestApiRegistered(
            @Param("id") Long id, 
            @Param("registered") Boolean registered, 
            @Param("status") String status);

    /**
     * 更新 GraphQL 注册状态
     */
    @Update("UPDATE api_generation_status SET graphql_registered = #{registered}, status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateGraphqlRegistered(
            @Param("id") Long id, 
            @Param("registered") Boolean registered, 
            @Param("status") String status);

    /**
     * 更新错误状态
     */
    @Update("UPDATE api_generation_status SET status = 'error', error_message = #{errorMessage}, error_stack = #{errorStack}, updated_at = NOW() WHERE id = #{id}")
    int updateError(
            @Param("id") Long id, 
            @Param("errorMessage") String errorMessage, 
            @Param("errorStack") String errorStack);

    /**
     * 重置所有生成状态
     */
    @Update("UPDATE api_generation_status SET status = 'pending', rest_api_registered = 0, graphql_registered = 0, updated_at = NOW() WHERE datasource_id = #{datasourceId}")
    int resetByDatasourceId(@Param("datasourceId") Long datasourceId);

    /**
     * 统计已生成的 API 数量
     */
    @Select("SELECT COUNT(*) FROM api_generation_status WHERE datasource_id = #{datasourceId} AND status = 'generated'")
    int countGeneratedByDatasourceId(@Param("datasourceId") Long datasourceId);

    /**
     * 统计错误数量
     */
    @Select("SELECT COUNT(*) FROM api_generation_status WHERE datasource_id = #{datasourceId} AND status = 'error'")
    int countErrorByDatasourceId(@Param("datasourceId") Long datasourceId);
}
