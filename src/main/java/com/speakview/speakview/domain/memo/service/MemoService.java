package com.speakview.speakview.domain.memo.service;

import com.speakview.speakview.domain.ai.entity.Content;
import com.speakview.speakview.domain.ai.repository.ContentRepository;
import com.speakview.speakview.domain.memo.dto.MemoCreateRequest;
import com.speakview.speakview.domain.memo.dto.MemoResponse;
import com.speakview.speakview.domain.memo.entity.Memo;
import com.speakview.speakview.domain.memo.repository.MemoRepository;
import com.speakview.speakview.global.exception.CustomException;
import com.speakview.speakview.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoService {

    private final MemoRepository memoRepository;
    private final ContentRepository contentRepository;

    @Transactional
    public MemoResponse createMemo(Long contentId, MemoCreateRequest request) {
        // 스크립트가 존재하는지(시작됐는지) 확인
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.CONTENT_NOT_FOUND, "contentId=" + contentId)
                );

        // 요청 데이터 기반으로 memo 생성
        Memo memo = Memo.builder()
                .content(content)
                .memoText(request.getMemoText())
                .timestamp(request.getTimestamp())
                .build();

        Memo savedMemo = memoRepository.save(memo);

        return MemoResponse.from(savedMemo);
    }

    @Transactional(readOnly = true)
    public List<MemoResponse> getMemos(Long contentId) {
        List<Memo> memos = memoRepository.findByContentIdOrderByIdAsc(contentId);

        return memos.stream()
                .map(MemoResponse::from)
                .collect(Collectors.toList());
    }
}