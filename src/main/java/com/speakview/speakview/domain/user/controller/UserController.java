package com.speakview.speakview.domain.user.controller;

import com.speakview.speakview.domain.user.dto.UserLoginRequest;
import com.speakview.speakview.domain.user.dto.UserResponse;
import com.speakview.speakview.domain.user.dto.UserSignupRequest;
import com.speakview.speakview.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public UserResponse signup(@RequestBody UserSignupRequest request) {
        return userService.signup(request);
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody UserLoginRequest request) {
        return userService.login(request);
    }
}