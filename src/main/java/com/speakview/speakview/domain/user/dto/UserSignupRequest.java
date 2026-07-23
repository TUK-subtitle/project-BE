package com.speakview.speakview.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserSignupRequest {

    @Schema(description = "로그인 ID", example = "tester")
    private String loginId;

    @Schema(description = "비밀번호", example = "password123")
    private String password;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;
}
