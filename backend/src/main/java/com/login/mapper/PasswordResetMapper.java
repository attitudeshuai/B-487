package com.login.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.login.entity.PasswordReset;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PasswordResetMapper extends BaseMapper<PasswordReset> {
}
