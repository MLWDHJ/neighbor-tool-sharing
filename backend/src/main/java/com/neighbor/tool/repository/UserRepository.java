package com.neighbor.tool.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neighbor.tool.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
}
