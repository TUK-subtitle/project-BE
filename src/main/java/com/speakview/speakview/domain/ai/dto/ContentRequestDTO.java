package com.speakview.speakview.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ContentRequestDTO {

    @Schema(description = "사용자(User) ID", example = "1")
    private Long userId;

    @Schema(description = "과목(Subject) ID", example = "1")
    private Long subjectId;
}