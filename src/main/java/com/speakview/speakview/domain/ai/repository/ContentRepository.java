package com.speakview.speakview.domain.ai.repository;

import com.speakview.speakview.domain.ai.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {

}