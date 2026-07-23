package com.speakview.speakview.domain.user.controller;

import com.speakview.speakview.domain.user.dto.UserLoginRequest;
import com.speakview.speakview.domain.user.dto.UserResponse;
import com.speakview.speakview.domain.user.dto.UserSignupRequest;
import com.speakview.speakview.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(
            summary = "회원가입 API",
            description = "로그인 ID, 비밀번호, 이름, 전화번호를 받아 새로운 사용자를 생성"
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "회원가입이 완료되고, 생성된 사용자 정보가 반환됩니다."
    ))
    public UserResponse signup(@RequestBody UserSignupRequest request) {
        return userService.signup(request);
    }

    @PostMapping("/login")
    @Operation(
            summary = "로그인 API",
            description = "로그인 ID와 비밀번호로 인증"
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인에 성공하면 사용자 정보가 반환됩니다."
    ))
    public UserResponse login(@RequestBody UserLoginRequest request) {
        return userService.login(request);
    }
}
