package com.login.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

public class VerificationCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String email;

    private String code;

    private String type;

    private LocalDateTime expireTime;

    private Integer used;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public VerificationCode() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getUsed() {
        return used;
    }

    public void setUsed(Integer used) {
        this.used = used;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String code;
        private String type;
        private LocalDateTime expireTime;
        private Integer used;
        private LocalDateTime createTime;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder expireTime(LocalDateTime expireTime) {
            this.expireTime = expireTime;
            return this;
        }

        public Builder used(Integer used) {
            this.used = used;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public VerificationCode build() {
            VerificationCode code = new VerificationCode();
            code.setId(this.id);
            code.setEmail(this.email);
            code.setCode(this.code);
            code.setType(this.type);
            code.setExpireTime(this.expireTime);
            code.setUsed(this.used);
            code.setCreateTime(this.createTime);
            return code;
        }
    }
}
