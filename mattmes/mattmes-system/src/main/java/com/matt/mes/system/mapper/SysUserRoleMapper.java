package com.matt.mes.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.matt.mes.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联Mapper
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
