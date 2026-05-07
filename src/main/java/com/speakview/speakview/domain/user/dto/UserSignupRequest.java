package com.speakview.speakview.domain.user.dto;

import lombok.Getter;

@Getter
public class UserSignupRequest {
    private String loginId;
    private String password;
    private String name;
    private String phoneNumber;
}