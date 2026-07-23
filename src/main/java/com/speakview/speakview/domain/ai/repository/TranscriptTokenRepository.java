package com.speakview.speakview.domain.ai.repository;

import com.speakview.speakview.domain.ai.entity.TranscriptToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranscriptTokenRepository extends JpaRepository<TranscriptToken, Long> {

    List<TranscriptToken> findAllByContentIdOrderBySeqAsc(Long contentId);

    List<TranscriptToken> findAllByContentIdAndStartMsBetweenOrderByStartMsAsc(Long contentId, Long fromMs, Long toMs);

    boolean existsByContentId(Long contentId);

    long countByContentId(Long contentId);
}