package com.speakview.speakview.domain.user.dto;

import lombok.Getter;

@Getter
public class UserLoginRequest {
    private String loginId;
    private String password;
}