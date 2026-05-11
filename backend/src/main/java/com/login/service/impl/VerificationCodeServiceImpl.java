package com.login.service.impl;

import com.login.common.ResultCode;
import com.login.exception.BusinessException;
import com.login.service.VerificationCodeService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeServiceImpl.class);

    private static final int CODE_EXPIRE_MINUTES = 10;
    private static final int SEND_INTERVAL_SECONDS = 60;
    private static final int CODE_LENGTH = 6;

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    private final Map<String, CodeEntry> codeStorage = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastSendTime = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public VerificationCodeServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::cleanExpiredCodes, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public boolean sendVerificationCode(String email) {
        log.info("发送验证码: email={}", email);

        LocalDateTime lastSend = lastSendTime.get(email);
        if (lastSend != null && LocalDateTime.now().isBefore(lastSend.plusSeconds(SEND_INTERVAL_SECONDS))) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码发送过于频繁，请60秒后重试");
        }

        String code = generateCode();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES);

        codeStorage.put(email, new CodeEntry(code, expireTime, false));
        lastSendTime.put(email, LocalDateTime.now());

        try {
            sendEmail(email, code);
            log.info("验证码发送成功: email={}", email);
            return true;
        } catch (Exception e) {
            log.error("验证码发送失败: email={}", email, e);
            codeStorage.remove(email);
            lastSendTime.remove(email);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "验证码发送失败，请稍后重试");
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        CodeEntry entry = codeStorage.get(email);
        if (entry == null) {
            return false;
        }
        if (entry.isUsed()) {
            return false;
        }
        if (LocalDateTime.now().isAfter(entry.getExpireTime())) {
            codeStorage.remove(email);
            return false;
        }
        return entry.getCode().equals(code);
    }

    @Override
    public boolean verifyAndInvalidateCode(String email, String code) {
        CodeEntry entry = codeStorage.get(email);
        if (entry == null) {
            return false;
        }
        if (entry.isUsed()) {
            return false;
        }
        if (LocalDateTime.now().isAfter(entry.getExpireTime())) {
            codeStorage.remove(email);
            return false;
        }
        if (entry.getCode().equals(code)) {
            entry.setUsed(true);
            codeStorage.remove(email);
            return true;
        }
        return false;
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("密码重置验证码");
        message.setText("您的密码重置验证码是：" + code + "\n\n" +
                "该验证码10分钟内有效，请勿泄露给他人。\n" +
                "如果不是您本人操作，请忽略此邮件。");
        mailSender.send(message);
    }

    private void cleanExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        codeStorage.entrySet().removeIf(entry -> 
            now.isAfter(entry.getValue().getExpireTime()) || entry.getValue().isUsed()
        );
        lastSendTime.entrySet().removeIf(entry -> 
            now.isAfter(entry.getValue().plusSeconds(SEND_INTERVAL_SECONDS + 10))
        );
    }

    private static class CodeEntry {
        private final String code;
        private final LocalDateTime expireTime;
        private boolean used;

        public CodeEntry(String code, LocalDateTime expireTime, boolean used) {
            this.code = code;
            this.expireTime = expireTime;
            this.used = used;
        }

        public String getCode() {
            return code;
        }

        public LocalDateTime getExpireTime() {
            return expireTime;
        }

        public boolean isUsed() {
            return used;
        }

        public void setUsed(boolean used) {
            this.used = used;
        }
    }
}
