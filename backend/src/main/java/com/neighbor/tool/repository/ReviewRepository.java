package com.neighbor.tool.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neighbor.tool.model.entity.Review;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewRepository extends BaseMapper<Review> {
}
