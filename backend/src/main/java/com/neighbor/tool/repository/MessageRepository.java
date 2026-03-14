package com.neighbor.tool.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neighbor.tool.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息数据访问层
 */
@Mapper
public interface MessageRepository extends BaseMapper<Message> {
}
