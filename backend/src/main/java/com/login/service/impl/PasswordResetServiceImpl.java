package com.login.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.login.common.ResultCode;
import com.login.dto.ResetPasswordDTO;
import com.login.dto.SendCodeDTO;
import com.login.dto.VerifyCodeDTO;
import com.login.entity.PasswordReset;
import com.login.entity.User;
import com.login.exception.BusinessException;
import com.login.mapper.PasswordResetMapper;
import com.login.mapper.UserMapper;
import com.login.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class PasswordResetServiceImpl extends ServiceImpl<PasswordResetMapper, PasswordReset> implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 10;
    private static final int CODE_SEND_INTERVAL_SECONDS = 60;
    private static final int RESET_TOKEN_BYTES = 32;

    private final UserMapper userMapper;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetServiceImpl(UserMapper userMapper, JavaMailSender mailSender, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendCode(SendCodeDTO sendCodeDTO) {
        String email = sendCodeDTO.getEmail();
        log.info("发送验证码请求: email={}", email);

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getEmail, email);
        User user = userMapper.selectOne(userWrapper);
        if (user == null) {
            throw new BusinessException(ResultCode.EMAIL_NOT_REGISTERED);
        }

        LambdaQueryWrapper<PasswordReset> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.eq(PasswordReset::getEmail, email)
                .orderByDesc(PasswordReset::getCreateTime)
                .last("LIMIT 1");
        PasswordReset lastRecord = getOne(recentWrapper);
        if (lastRecord != null && lastRecord.getCreateTime() != null) {
            LocalDateTime nextAllowedTime = lastRecord.getCreateTime().plusSeconds(CODE_SEND_INTERVAL_SECONDS);
            if (LocalDateTime.now().isBefore(nextAllowedTime)) {
                throw new BusinessException(ResultCode.CODE_SEND_TOO_FREQUENT);
            }
        }

        String code = generateCode();

        PasswordReset resetRecord = new PasswordReset();
        resetRecord.setEmail(email);
        resetRecord.setCode(code);
        resetRecord.setUsed(0);
        resetRecord.setExpireTime(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        resetRecord.setCreateTime(LocalDateTime.now());
        save(resetRecord);

        sendEmail(email, code);

        log.info("验证码已发送: email={}", email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String verifyCode(VerifyCodeDTO verifyCodeDTO) {
        String email = verifyCodeDTO.getEmail();
        String code = verifyCodeDTO.getCode();
        log.info("校验验证码: email={}", email);

        PasswordReset record = findValidCode(email, code);
        if (record == null) {
            throw new BusinessException(ResultCode.CODE_INVALID);
        }

        record.setUsed(1);
        String resetToken = generateResetToken();
        record.setResetToken(resetToken);
        updateById(record);

        log.info("验证码校验成功，已生成resetToken: email={}", email);
        return resetToken;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        String resetToken = resetPasswordDTO.getResetToken();
        String newPassword = resetPasswordDTO.getNewPassword();
        log.info("重置密码请求: resetToken={}...", resetToken != null ? resetToken.substring(0, 8) : "null");

        LambdaQueryWrapper<PasswordReset> tokenWrapper = new LambdaQueryWrapper<>();
        tokenWrapper.eq(PasswordReset::getResetToken, resetToken);
        PasswordReset record = getOne(tokenWrapper);

        if (record == null) {
            throw new BusinessException(ResultCode.CODE_INVALID, "无效的重置凭证");
        }

        if (record.getExpireTime() != null && LocalDateTime.now().isAfter(record.getExpireTime())) {
            throw new BusinessException(ResultCode.CODE_INVALID, "重置凭证已过期，请重新获取验证码");
        }

        record.setResetToken(null);
        updateById(record);

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getEmail, record.getEmail());
        User user = userMapper.selectOne(userWrapper);
        if (user == null) {
            throw new BusinessException(ResultCode.EMAIL_NOT_REGISTERED);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("密码重置成功: email={}, userId={}", record.getEmail(), user.getId());
    }

    private PasswordReset findValidCode(String email, String code) {
        LambdaQueryWrapper<PasswordReset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PasswordReset::getEmail, email)
                .eq(PasswordReset::getCode, code)
                .eq(PasswordReset::getUsed, 0)
                .orderByDesc(PasswordReset::getCreateTime)
                .last("LIMIT 1");
        PasswordReset record = getOne(wrapper);

        if (record == null) {
            return null;
        }

        if (LocalDateTime.now().isAfter(record.getExpireTime())) {
            return null;
        }

        return record;
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateResetToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("密码重置验证码");
        message.setText("您的验证码为：" + code + "，有效期10分钟，请勿泄露给他人。");
        mailSender.send(message);
    }
}
