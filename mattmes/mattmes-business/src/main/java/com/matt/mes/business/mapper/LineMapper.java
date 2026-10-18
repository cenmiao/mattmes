package com.matt.mes.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.matt.mes.business.entity.MesLine;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 线体Mapper接口
 */
public interface LineMapper extends BaseMapper<MesLine> {

    /**
     * 查询指定编码的记录数量（包括已删除的记录）。
     * 用于唯一性校验：逻辑删除会让默认查询忽略已删除记录，
     * 若只查未删除数据，删除某编码后重建会撞上数据库唯一索引。
     * 使用原生 SQL 绕开 @TableLogic 的自动追加条件。
     *
     * @param code 线体编码
     * @return 记录数量
     */
    @Select("SELECT COUNT(*) FROM mes_line WHERE line_code = #{code}")
    Long countByCodeIncludeDeleted(@Param("code") String code);
}
