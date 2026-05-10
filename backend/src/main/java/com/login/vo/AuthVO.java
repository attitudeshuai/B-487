package com.login.vo;

import java.io.Serializable;

/**
 * 认证响应 VO
 * 
 * @author Login System
 */
public class AuthVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Access Token
     */
    private String token;

    /**
     * Refresh Token
     */
    private String refreshToken;

    /**
     * Token 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * Token 类型
     */
    private String tokenType = "Bearer";

    /**
     * 用户信息
     */
    private UserVO user;

    public AuthVO() {
    }

    public AuthVO(String token, String refreshToken, Long expiresIn, UserVO user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String refreshToken;
        private Long expiresIn;
        private UserVO user;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public Builder user(UserVO user) {
            this.user = user;
            return this;
        }

        public AuthVO build() {
            return new AuthVO(token, refreshToken, expiresIn, user);
        }
    }
}
