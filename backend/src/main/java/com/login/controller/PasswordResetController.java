package com.login.controller;

import com.login.common.Result;
import com.login.dto.ResetPasswordDTO;
import com.login.dto.SendCodeDTO;
import com.login.dto.VerifyCodeDTO;
import com.login.service.PasswordResetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeDTO sendCodeDTO) {
        log.info("发送验证码请求: email={}", sendCodeDTO.getEmail());
        passwordResetService.sendCode(sendCodeDTO);
        return Result.success("验证码已发送", null);
    }

    @PostMapping("/verify-code")
    public Result<Map<String, String>> verifyCode(@Valid @RequestBody VerifyCodeDTO verifyCodeDTO) {
        log.info("校验验证码请求: email={}", verifyCodeDTO.getEmail());
        String resetToken = passwordResetService.verifyCode(verifyCodeDTO);
        return Result.success("验证码校验通过", Map.of("resetToken", resetToken));
    }

    @PostMapping("/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        log.info("重置密码请求");
        passwordResetService.resetPassword(resetPasswordDTO);
        return Result.success("密码重置成功", null);
    }
}
