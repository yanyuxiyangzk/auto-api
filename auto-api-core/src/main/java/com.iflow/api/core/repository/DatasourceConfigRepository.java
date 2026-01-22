package com.iflow.api.core.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iflow.api.core.entity.DatasourceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 数据源配置 Repository
 * 
 * 引用：REQ-F4-002（动态添加/删除数据源）
 */
@Mapper
public interface DatasourceConfigRepository extends BaseMapper<DatasourceConfig> {

    /**
     * 根据 ID 查询（排除已删除）
     */
    @Select("SELECT * FROM api_datasource_config WHERE id = #{id} AND deleted = 0")
    Optional<DatasourceConfig> findByIdWithoutDeleted(@Param("id") Long id);

    /**
     * 查询所有启用的数据源
     */
    @Select("SELECT * FROM api_datasource_config WHERE status = 1 AND deleted = 0 ORDER BY is_primary DESC, id ASC")
    List<DatasourceConfig> findAllActive();

    /**
     * 根据数据源类型查询
     */
    @Select("SELECT * FROM api_datasource_config WHERE type = #{type} AND deleted = 0 ORDER BY is_primary DESC")
    List<DatasourceConfig> findByType(@Param("type") String type);

    /**
     * 根据名称查询
     */
    @Select("SELECT * FROM api_datasource_config WHERE name = #{name} AND deleted = 0")
    Optional<DatasourceConfig> findByName(@Param("name") String name);

    /**
     * 检查 ID 是否存在（排除已删除）
     */
    @Select("SELECT COUNT(*) FROM api_datasource_config WHERE id = #{id} AND deleted = 0")
    boolean existsByIdWithoutDeleted(@Param("id") Long id);

    /**
     * 检查名称是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM api_datasource_config WHERE name = #{name} AND deleted = 0")
    boolean existsByName(@Param("name") String name);

    /**
     * 逻辑删除
     */
    @Select("UPDATE api_datasource_config SET deleted = 1, updated_at = NOW() WHERE id = #{id}")
    int logicalDelete(@Param("id") Long id);

    /**
     * 更新状态
     */
    @Select("UPDATE api_datasource_config SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
