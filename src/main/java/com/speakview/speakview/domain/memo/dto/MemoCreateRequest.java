package com.speakview.speakview.domain.memo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemoCreateRequest {

    @Schema(description = "메모 내용", example = "여기 시험에 나올 수도 있음")
    @NotBlank(message = "memoText는 비어있을 수 없습니다.")
    private String memoText;

    @Schema(description = "강의 재생 시점 타임스탬프", example = "00:12:34")
    @NotNull(message = "timestamp 값이 필요합니다.")
    private String timestamp;
}
