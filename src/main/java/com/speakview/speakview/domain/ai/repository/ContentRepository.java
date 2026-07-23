package com.speakview.speakview.domain.ai.repository;

import com.speakview.speakview.domain.ai.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Content> findByUser_IdAndSubject_NameOrderByCreatedAtDesc(Long userId, String subjectName);
}