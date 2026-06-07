package com.matt.mes.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.matt.mes.business.entity.MesMaterial;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 料号Mapper接口
 */
public interface MaterialMapper extends BaseMapper<MesMaterial> {

    /**
     * 查询指定编码的记录数量（包括已删除的记录）
     * 用于唯一性校验，避免数据库唯一索引冲突
     *
     * @param code 料号编码
     * @return 记录数量
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT COUNT(*) FROM mes_material WHERE material_code = #{code}")
    Long countByCodeIncludeDeleted(@Param("code") String code);
}