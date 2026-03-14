package com.neighbor.tool.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neighbor.tool.model.entity.Faq;
import org.apache.ibatis.annotations.Mapper;

/**
 * FAQ Repository
 */
@Mapper
public interface FaqRepository extends BaseMapper<Faq> {
}
