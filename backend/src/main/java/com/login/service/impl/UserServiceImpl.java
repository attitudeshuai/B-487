package com.login.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.login.common.ResultCode;
import com.login.dto.LoginDTO;
import com.login.dto.RegisterDTO;
import com.login.entity.User;
import com.login.exception.BusinessException;
import com.login.mapper.UserMapper;
import com.login.service.UserService;
import com.login.util.JwtTokenUtil;
import com.login.vo.AuthVO;
import com.login.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 用户服务实现类
 * 
 * @author Login System
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 旧密码加盐（仅用于兼容旧用户迁移）
     */
    private static final String LEGACY_SALT = "login_system_salt_2024";

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public UserServiceImpl(PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthVO login(LoginDTO loginDTO) {
        log.info("用户登录: username={}", loginDTO.getUsername());
        
        // 根据用户名查询用户
        User user = getByUsername(loginDTO.getUsername());
        if (user == null) {
            log.warn("用户不存在: username={}", loginDTO.getUsername());
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("用户已被禁用: username={}", loginDTO.getUsername());
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        
        // 验证密码
        boolean passwordMatches = false;
        String inputPassword = loginDTO.getPassword();
        
        // 首先尝试 BCrypt 验证
        if (user.getPassword().startsWith("$2")) {
            passwordMatches = passwordEncoder.matches(inputPassword, user.getPassword());
        } else {
            // 兼容旧的 MD5 密码，验证后自动迁移到 BCrypt
            String legacyEncryptedPassword = encryptLegacyPassword(inputPassword);
            if (legacyEncryptedPassword.equals(user.getPassword())) {
                passwordMatches = true;
                // 迁移密码到 BCrypt
                user.setPassword(passwordEncoder.encode(inputPassword));
                user.setUpdateTime(LocalDateTime.now());
                updateById(user);
                log.info("用户密码已迁移到BCrypt: userId={}", user.getId());
            }
        }
        
        if (!passwordMatches) {
            log.warn("密码错误: username={}", loginDTO.getUsername());
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        
        // 生成 JWT Token
        String accessToken = jwtTokenUtil.generateToken(user);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user);
        
        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        
        return AuthVO.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenUtil.getExpirationSeconds())
                .user(convertToVO(user))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthVO register(RegisterDTO registerDTO) {
        log.info("用户注册: username={}", registerDTO.getUsername());
        
        // 检查用户名是否已存在
        User existingUser = getByUsername(registerDTO.getUsername());
        if (existingUser != null) {
            log.warn("用户名已存在: username={}", registerDTO.getUsername());
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        
        // 检查邮箱是否已被使用
        if (StringUtils.hasText(registerDTO.getEmail())) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, registerDTO.getEmail());
            if (count(emailWrapper) > 0) {
                log.warn("邮箱已被注册: email={}", registerDTO.getEmail());
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
        }
        
        // 创建新用户（使用 BCrypt 加密密码）
        User user = User.builder()
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .nickname(StringUtils.hasText(registerDTO.getNickname()) ? registerDTO.getNickname() : registerDTO.getUsername())
                .email(registerDTO.getEmail())
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        // 保存用户
        boolean saved = save(user);
        if (!saved) {
            log.error("用户注册失败: username={}", registerDTO.getUsername());
            throw new BusinessException(ResultCode.REGISTER_FAILED);
        }
        
        // 生成 JWT Token
        String accessToken = jwtTokenUtil.generateToken(user);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user);
        
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
        
        return AuthVO.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenUtil.getExpirationSeconds())
                .user(convertToVO(user))
                .build();
    }

    @Override
    public AuthVO refreshToken(String refreshToken) {
        log.debug("刷新Token");
        
        // 验证 Refresh Token
        if (!jwtTokenUtil.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "无效的刷新令牌");
        }
        
        // 检查是否为 Refresh Token
        if (!jwtTokenUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "非刷新令牌");
        }
        
        // 获取用户ID
        Long userId = jwtTokenUtil.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "无效的刷新令牌");
        }
        
        // 获取用户信息
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        
        // 生成新的 Token
        String newAccessToken = jwtTokenUtil.generateToken(user);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(user);
        
        log.info("Token刷新成功: userId={}", userId);
        
        return AuthVO.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtTokenUtil.getExpirationSeconds())
                .user(convertToVO(user))
                .build();
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        log.debug("获取用户信息: userId={}", userId);
        
        User user = getById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        return convertToVO(user);
    }

    @Override
    public UserVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return convertToVO(user);
        }
        
        throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }

    /**
     * 旧密码加密（MD5 + 盐，仅用于兼容迁移）
     */
    private String encryptLegacyPassword(String password) {
        String saltedPassword = password + LEGACY_SALT;
        return DigestUtils.md5DigestAsHex(saltedPassword.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 将用户实体转换为VO
     *
     * @param user 用户实体
     * @return 用户VO
     */
    private UserVO convertToVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .createTime(user.getCreateTime())
                .build();
    }
}
