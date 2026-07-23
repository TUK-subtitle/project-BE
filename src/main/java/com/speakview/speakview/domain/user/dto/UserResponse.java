package com.speakview.speakview.domain.user.dto;

import com.speakview.speakview.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    @Schema(description = "사용자(User) ID", example = "1")
    private Long id;

    @Schema(description = "로그인 ID", example = "tester")
    private String loginId;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .build();
    }
}
