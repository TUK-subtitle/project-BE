package com.speakview.speakview.domain.ai.service;

import com.speakview.speakview.domain.ai.entity.Content;
import com.speakview.speakview.domain.ai.entity.TranscriptFull;
import com.speakview.speakview.domain.ai.entity.TranscriptToken;
import com.speakview.speakview.domain.ai.repository.ContentRepository;
import com.speakview.speakview.domain.ai.repository.TranscriptFullRepository;
import com.speakview.speakview.domain.ai.repository.TranscriptTokenRepository;
import com.speakview.speakview.global.exception.CustomException;
import com.speakview.speakview.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TranscriptService {

    private final ContentRepository contentRepository;
    private final TranscriptTokenRepository transcriptTokenRepository;
    private final TranscriptFullRepository transcriptFullRepository;

    @Transactional
    public TranscriptToken saveToken(
            Long contentId,
            String text,
            Integer speaker,
            Long startMs,
            Long endMs,
            Double confidence
    ) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND, "contentId=" + contentId));

        long nextSeq = transcriptTokenRepository.countByContentId(contentId) + 1L;

        TranscriptToken token = TranscriptToken.builder()
                .user(content.getUser())
                .content(content)
                .seq(nextSeq)
                .text(text)
                .speaker(speaker)
                .startMs(startMs)
                .endMs(endMs)
                .confidence(confidence)
                .build();

        TranscriptToken saved = transcriptTokenRepository.save(token);
        appendFullText(content, text);

        return saved;
    }

    @Transactional
    public void appendFullText(Long contentId, String tokenText) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND, "contentId=" + contentId));

        appendFullText(content, tokenText);
    }

    private void appendFullText(Content content, String tokenText) {
        if (tokenText == null || tokenText.isBlank()) return;

        TranscriptFull transcriptFull = transcriptFullRepository.findByContentId(content.getId())
                .orElseGet(() -> TranscriptFull.builder()
                        .user(content.getUser())
                        .content(content)
                        .fullText("")
                        .tokenCount(0L)
                        .build());

        String current = transcriptFull.getFullText() == null ? "" : transcriptFull.getFullText();
        String appended = current.isBlank() ? tokenText : current + " " + tokenText;

        TranscriptFull updated = TranscriptFull.builder()
                .id(transcriptFull.getId())
                .user(transcriptFull.getUser())
                .content(transcriptFull.getContent())
                .fullText(appended)
                .tokenCount(transcriptFull.getTokenCount() + 1L)
                .build();

        transcriptFullRepository.save(updated);
    }
}