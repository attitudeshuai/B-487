package com.login.service;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {

    /**
     * 发送验证码
     *
     * @param email 邮箱
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String email);

    /**
     * 验证验证码
     *
     * @param email 邮箱
     * @param code  验证码
     * @return 是否验证成功
     */
    boolean verifyCode(String email, String code);

    /**
     * 验证并失效验证码（验证成功后立即失效，不可复用）
     *
     * @param email 邮箱
     * @param code  验证码
     * @return 是否验证成功
     */
    boolean verifyAndInvalidateCode(String email, String code);
}
