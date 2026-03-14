package com.neighbor.tool.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neighbor.tool.model.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * Feedback Repository
 */
@Mapper
public interface FeedbackRepository extends BaseMapper<Feedback> {
}
