package com.speakview.speakview.domain.memo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemoCreateRequest {

    @NotBlank(message = "memoText는 비어있을 수 없습니다.")
    private String memoText;

    @NotNull(message = "timestamp 값이 필요합니다.")
    private String timestamp;
}