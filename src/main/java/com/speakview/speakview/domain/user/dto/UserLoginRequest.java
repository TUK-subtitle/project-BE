package com.speakview.speakview.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserLoginRequest {

    @Schema(description = "로그인 ID", example = "tester")
    private String loginId;

    @Schema(description = "비밀번호", example = "password123")
    private String password;
}
