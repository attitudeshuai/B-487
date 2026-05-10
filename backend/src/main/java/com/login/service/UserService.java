package com.login.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.login.dto.LoginDTO;
import com.login.dto.RegisterDTO;
import com.login.entity.User;
import com.login.vo.AuthVO;
import com.login.vo.UserVO;

/**
 * 用户服务接口
 * 
 * @author Login System
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求
     * @return 认证信息（包含Token）
     */
    AuthVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     *
     * @param registerDTO 注册请求
     * @return 认证信息（包含Token）
     */
    AuthVO register(RegisterDTO registerDTO);

    /**
     * 刷新 Token
     *
     * @param refreshToken 刷新Token
     * @return 新的认证信息
     */
    AuthVO refreshToken(String refreshToken);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserInfo(Long userId);

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    UserVO getCurrentUser();

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    User getByUsername(String username);
}

