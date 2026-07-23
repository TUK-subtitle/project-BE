package com.speakview.speakview.domain.ai.repository;

import com.speakview.speakview.domain.ai.entity.LectureAudio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LectureAudioRepository extends JpaRepository<LectureAudio, Long> {

    Optional<LectureAudio> findFirstByContentIdOrderByCreatedAtDesc(Long contentId);

    boolean existsByContentId(Long contentId);
}