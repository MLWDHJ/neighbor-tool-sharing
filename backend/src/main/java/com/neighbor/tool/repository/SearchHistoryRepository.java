package com.neighbor.tool.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neighbor.tool.model.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SearchHistoryRepository extends BaseMapper<SearchHistory> {
}
