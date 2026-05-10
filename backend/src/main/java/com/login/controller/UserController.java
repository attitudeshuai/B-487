package com.login.controller;

import com.login.common.Result;
import com.login.dto.LoginDTO;
import com.login.dto.RegisterDTO;
import com.login.service.UserService;
import com.login.vo.AuthVO;
import com.login.vo.UserVO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 
 * @author Login System
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     * 
     * @param loginDTO 登录请求（JSON格式）
     * @return 认证信息（包含Token）
     */
    @PostMapping("/login")
    public Result<AuthVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("收到登录请求: username={}", loginDTO.getUsername());
        AuthVO authVO = userService.login(loginDTO);
        return Result.success("登录成功", authVO);
    }

    /**
     * 用户注册
     * 
     * @param registerDTO 注册请求（JSON格式）
     * @return 认证信息（包含Token）
     */
    @PostMapping("/register")
    public Result<AuthVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        log.info("收到注册请求: username={}", registerDTO.getUsername());
        AuthVO authVO = userService.register(registerDTO);
        return Result.success("注册成功", authVO);
    }

    /**
     * 刷新 Token
     * 
     * @param refreshToken 刷新令牌
     * @return 新的认证信息
     */
    @PostMapping("/refresh")
    public Result<AuthVO> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.debug("刷新Token请求");
        AuthVO authVO = userService.refreshToken(request.getRefreshToken());
        return Result.success("刷新成功", authVO);
    }

    /**
     * 获取当前登录用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        log.debug("获取当前用户信息");
        UserVO userVO = userService.getCurrentUser();
        return Result.success(userVO);
    }

    /**
     * 获取用户信息
     * 
     * @param id 用户ID（路径参数）
     * @return 用户信息
     */
    @GetMapping("/info/{id}")
    public Result<UserVO> getUserInfo(@PathVariable("id") Long id) {
        log.debug("获取用户信息: userId={}", id);
        UserVO userVO = userService.getUserInfo(id);
        return Result.success(userVO);
    }

    /**
     * 用户登出
     * 
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        log.info("用户登出");
        // JWT 无状态，登出由前端清除 Token 实现
        return Result.success("登出成功", null);
    }

    /**
     * 刷新Token请求体
     */
    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
