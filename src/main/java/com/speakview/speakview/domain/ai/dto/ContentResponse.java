package com.speakview.speakview.domain.ai.dto;

import com.speakview.speakview.domain.ai.entity.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ContentResponse {

    @Schema(description = "강의(Content) ID", example = "1")
    private Long id;

    @Schema(description = "강의 제목", example = "3주차 강의")
    private String title;

    @Schema(description = "강의 생성 일시 (한국 표준시, KST)", example = "2026-07-23T22:25:46")
    private LocalDateTime createdAt;

    public static ContentResponse from(Content content) {
        return ContentResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .createdAt(content.getCreatedAt())
                .build();
    }
}
