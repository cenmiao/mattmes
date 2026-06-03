package com.matt.mes.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.matt.mes.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 检查工号是否存在（包括已逻辑删除的用户）
     * 用于创建用户前的唯一性校验
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE user_no = #{userNo}")
    long countByUserNoIncludingDeleted(@Param("userNo") String userNo);
}
