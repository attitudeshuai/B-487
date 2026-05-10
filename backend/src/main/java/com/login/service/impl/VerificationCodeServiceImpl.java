package com.login.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.login.common.ResultCode;
import com.login.entity.User;
import com.login.entity.VerificationCode;
import com.login.exception.BusinessException;
import com.login.mapper.UserMapper;
import com.login.mapper.VerificationCodeMapper;
import com.login.service.VerificationCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class VerificationCodeServiceImpl extends ServiceImpl<VerificationCodeMapper, VerificationCode> implements VerificationCodeService {

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeServiceImpl.class);

    private static final String CODE_TYPE_PASSWORD_RESET = "PASSWORD_RESET";
    private static final int CODE_EXPIRE_MINUTES = 10;
    private static final int CODE_SEND_INTERVAL_SECONDS = 60;

    private final JavaMailSender mailSender;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.mail.from:no-reply@example.com}")
    private String mailFrom;

    public VerificationCodeServiceImpl(JavaMailSender mailSender, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.mailSender = mailSender;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendResetPasswordCode(String email) {
        log.info("发送重置密码验证码: email={}", email);

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getEmail, email);
        User user = userMapper.selectOne(userWrapper);
        if (user == null) {
            log.warn("邮箱未注册: email={}", email);
            throw new BusinessException(ResultCode.EMAIL_NOT_REGISTERED);
        }

        LambdaQueryWrapper<VerificationCode> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.eq(VerificationCode::getEmail, email)
                .eq(VerificationCode::getType, CODE_TYPE_PASSWORD_RESET)
                .orderByDesc(VerificationCode::getCreateTime)
                .last("LIMIT 1");
        VerificationCode recentCode = getOne(recentWrapper);

        if (recentCode != null && recentCode.getCreateTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime earliestAllowedTime = recentCode.getCreateTime().plusSeconds(CODE_SEND_INTERVAL_SECONDS);
            if (now.isBefore(earliestAllowedTime)) {
                log.warn("验证码发送过于频繁: email={}", email);
                throw new BusinessException(ResultCode.CODE_SEND_TOO_FREQUENT);
            }
        }

        String code = generateCode();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES);

        VerificationCode verificationCode = VerificationCode.builder()
                .email(email)
                .code(code)
                .type(CODE_TYPE_PASSWORD_RESET)
                .expireTime(expireTime)
                .used(0)
                .createTime(LocalDateTime.now())
                .build();

        boolean saved = save(verificationCode);
        if (!saved) {
            log.error("保存验证码失败: email={}", email);
            throw new BusinessException(ResultCode.CODE_SEND_FAILED);
        }

        try {
            sendEmail(email, code);
            log.info("验证码发送成功: email={}", email);
        } catch (Exception e) {
            log.error("发送邮件失败: email={}", email, e);
            throw new BusinessException(ResultCode.CODE_SEND_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyCode(String email, String code) {
        log.info("校验验证码: email={}", email);

        LambdaQueryWrapper<VerificationCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerificationCode::getEmail, email)
                .eq(VerificationCode::getCode, code)
                .eq(VerificationCode::getType, CODE_TYPE_PASSWORD_RESET)
                .orderByDesc(VerificationCode::getCreateTime)
                .last("LIMIT 1");

        VerificationCode verificationCode = getOne(wrapper);

        if (verificationCode == null) {
            log.warn("验证码不存在: email={}", email);
            throw new BusinessException(ResultCode.CODE_INVALID);
        }

        if (verificationCode.getUsed() != null && verificationCode.getUsed() == 1) {
            log.warn("验证码已被使用: email={}", email);
            throw new BusinessException(ResultCode.CODE_ALREADY_USED);
        }

        if (verificationCode.getExpireTime() == null || LocalDateTime.now().isAfter(verificationCode.getExpireTime())) {
            log.warn("验证码已过期: email={}", email);
            throw new BusinessException(ResultCode.CODE_INVALID);
        }

        verificationCode.setUsed(1);
        updateById(verificationCode);
        log.info("验证码校验成功，已标记为已使用: email={}", email);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String email, String code, String newPassword) {
        log.info("重置密码: email={}", email);

        LambdaQueryWrapper<VerificationCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerificationCode::getEmail, email)
                .eq(VerificationCode::getCode, code)
                .eq(VerificationCode::getType, CODE_TYPE_PASSWORD_RESET)
                .orderByDesc(VerificationCode::getCreateTime)
                .last("LIMIT 1");

        VerificationCode verificationCode = getOne(wrapper);

        if (verificationCode == null) {
            log.warn("验证码不存在: email={}", email);
            throw new BusinessException(ResultCode.CODE_INVALID);
        }

        if (verificationCode.getUsed() == null || verificationCode.getUsed() == 0) {
            log.warn("验证码未经过校验: email={}", email);
            throw new BusinessException(ResultCode.CODE_INVALID);
        }

        if (verificationCode.getExpireTime() == null || LocalDateTime.now().isAfter(verificationCode.getExpireTime())) {
            log.warn("验证码已过期: email={}", email);
            throw new BusinessException(ResultCode.CODE_INVALID);
        }

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getEmail, email);
        User user = userMapper.selectOne(userWrapper);
        if (user == null) {
            log.warn("用户不存在: email={}", email);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        int updated = userMapper.updateById(user);
        if (updated <= 0) {
            log.error("更新密码失败: email={}", email);
            throw new BusinessException(ResultCode.PASSWORD_RESET_FAILED);
        }

        log.info("密码重置成功: email={}", email);
    }

    private String generateCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject("密码重置验证码");
        message.setText("您的密码重置验证码是：" + code + "，10分钟内有效。如非本人操作，请忽略此邮件。");
        mailSender.send(message);
    }
}
