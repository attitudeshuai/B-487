package com.login.service;

public interface VerificationCodeService {

    /**
     * 发送重置密码验证码
     *
     * @param email 邮箱
     */
    void sendResetPasswordCode(String email);

    /**
     * 校验验证码
     *
     * @param email 邮箱
     * @param code  验证码
     * @return 校验是否通过
     */
    boolean verifyCode(String email, String code);

    /**
     * 重置密码
     *
     * @param email       邮箱
     * @param code        验证码
     * @param newPassword 新密码
     */
    void resetPassword(String email, String code, String newPassword);
}
