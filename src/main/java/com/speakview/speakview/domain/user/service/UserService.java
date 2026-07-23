package com.speakview.speakview.domain.user.service;

import com.speakview.speakview.domain.user.dto.UserLoginRequest;
import com.speakview.speakview.domain.user.dto.UserResponse;
import com.speakview.speakview.domain.user.dto.UserSignupRequest;
import com.speakview.speakview.domain.user.entity.User;
import com.speakview.speakview.domain.user.repository.UserRepository;
import com.speakview.speakview.global.exception.CustomException;
import com.speakview.speakview.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse signup(UserSignupRequest request) {

        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID, "loginId=" + request.getLoginId());
        }

        User user = User.builder()
                .loginId(request.getLoginId())
                .password(request.getPassword())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse login(UserLoginRequest request) {

        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        return UserResponse.from(user);
    }
}
