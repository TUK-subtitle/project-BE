package com.speakview.speakview.domain.memo.repository;

import com.speakview.speakview.domain.memo.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    List<Memo> findByContentIdOrderByIdAsc(Long contentId);
}