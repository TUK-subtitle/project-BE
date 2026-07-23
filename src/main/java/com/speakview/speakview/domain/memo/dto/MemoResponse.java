package com.speakview.speakview.domain.memo.dto;

import com.speakview.speakview.domain.memo.entity.Memo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemoResponse {

    @Schema(description = "메모 ID", example = "1")
    private Long id;

    @Schema(description = "강의(Content) ID", example = "1")
    private Long contentId;

    @Schema(description = "메모 내용", example = "여기 시험에 나올 수도 있음")
    private String memoText;

    @Schema(description = "강의 재생 시점 타임스탬프", example = "00:12:34")
    private String timestamp;

    public static MemoResponse from(Memo memo) {
        return MemoResponse.builder()
                .id(memo.getId())
                .contentId(memo.getContent().getId())
                .memoText(memo.getMemoText())
                .timestamp(memo.getTimestamp())
                .build();
    }
}
