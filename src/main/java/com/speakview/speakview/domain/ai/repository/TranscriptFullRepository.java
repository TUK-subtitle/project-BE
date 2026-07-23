package com.speakview.speakview.domain.ai.repository;

import com.speakview.speakview.domain.ai.entity.TranscriptFull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TranscriptFullRepository extends JpaRepository<TranscriptFull, Long> {

    Optional<TranscriptFull> findByContentId(Long contentId);

    boolean existsByContentId(Long contentId);
}