package com.speakview.speakview.domain.ai.service;

import com.speakview.speakview.domain.ai.entity.LectureAudio;
import com.speakview.speakview.domain.ai.entity.TranscriptFull;
import com.speakview.speakview.domain.ai.entity.TranscriptToken;
import com.speakview.speakview.domain.ai.repository.LectureAudioRepository;
import com.speakview.speakview.domain.ai.repository.TranscriptFullRepository;
import com.speakview.speakview.domain.ai.repository.TranscriptTokenRepository;
import com.speakview.speakview.global.exception.CustomException;
import com.speakview.speakview.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TranscriptQueryService {

    private final TranscriptFullRepository transcriptFullRepository;
    private final TranscriptTokenRepository transcriptTokenRepository;
    private final LectureAudioRepository lectureAudioRepository;

    public TranscriptFull getTranscriptFull(Long contentId) {
        return transcriptFullRepository.findByContentId(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND, "자막 본문이 없습니다. contentId=" + contentId));
    }

    public LectureAudio getLectureAudio(Long contentId) {
        return lectureAudioRepository.findFirstByContentIdOrderByCreatedAtDesc(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND, "오디오가 없습니다. contentId=" + contentId));
    }

    public List<TranscriptToken> getTokens(Long contentId) {
        return transcriptTokenRepository.findAllByContentIdOrderBySeqAsc(contentId);
    }

    public List<TranscriptToken> getTokens(Long contentId, Long fromMs, Long toMs) {
        if (fromMs == null || toMs == null) {
            return getTokens(contentId);
        }
        if (fromMs > toMs) {
            throw new IllegalArgumentException("fromMs는 toMs보다 클 수 없습니다.");
        }
        return transcriptTokenRepository.findAllByContentIdAndStartMsBetweenOrderByStartMsAsc(contentId, fromMs, toMs);
    }
}