package com.neighbor.tool.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neighbor.tool.model.entity.VerificationCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 验证码数据访问层
 */
@Mapper
public interface VerificationCodeRepository extends BaseMapper<VerificationCode> {
}
