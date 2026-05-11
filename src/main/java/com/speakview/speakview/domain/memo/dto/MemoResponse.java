package com.speakview.speakview.domain.memo.dto;

import com.speakview.speakview.domain.memo.entity.Memo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemoResponse {

    private Long id;
    private Long contentId;
    private String memoText;
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