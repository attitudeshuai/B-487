package com.login.service;

import com.login.dto.ResetPasswordDTO;
import com.login.dto.SendCodeDTO;
import com.login.dto.VerifyCodeDTO;

public interface PasswordResetService {

    void sendCode(SendCodeDTO sendCodeDTO);

    String verifyCode(VerifyCodeDTO verifyCodeDTO);

    void resetPassword(ResetPasswordDTO resetPasswordDTO);
}
