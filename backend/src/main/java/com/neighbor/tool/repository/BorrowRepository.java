package com.neighbor.tool.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neighbor.tool.model.entity.Borrow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 借用记录数据访问层
 */
@Mapper
public interface BorrowRepository extends BaseMapper<Borrow> {
}
