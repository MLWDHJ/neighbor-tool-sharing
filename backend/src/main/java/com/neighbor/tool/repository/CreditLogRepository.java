package com.neighbor.tool.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neighbor.tool.model.entity.CreditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CreditLogRepository extends BaseMapper<CreditLog> {
}
