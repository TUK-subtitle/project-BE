package com.speakview.speakview.domain.ai.repository;

import com.speakview.speakview.domain.ai.entity.Summary;
import com.speakview.speakview.domain.ai.enums.SummaryType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    List<Summary> findAllByContentIdAndTypeOrderByCreatedAtAsc(Long contentId, SummaryType summaryType);
    Optional<Summary> findFirstByContentIdAndTypeOrderByCreatedAtDesc(Long contentId, SummaryType summaryType);
}