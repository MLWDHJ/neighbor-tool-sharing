package com.neighbor.tool.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neighbor.tool.model.entity.Tool;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工具数据访问层
 */
@Mapper
public interface ToolRepository extends BaseMapper<Tool> {
}
