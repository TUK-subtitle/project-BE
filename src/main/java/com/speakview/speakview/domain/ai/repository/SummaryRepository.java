package com.speakview.speakview.domain.ai.repository;

import com.speakview.speakview.domain.ai.entity.Summary;
import com.speakview.speakview.domain.ai.enums.SummaryType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    List<Summary> findAllByContentIdAndSummaryTypeOrderByCreatedAtAsc(Long contentId, SummaryType summaryType);
}