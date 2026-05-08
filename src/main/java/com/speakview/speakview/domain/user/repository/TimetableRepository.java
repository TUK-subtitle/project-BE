package com.speakview.speakview.domain.user.repository;

import com.speakview.speakview.domain.user.entity.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimetableRepository extends JpaRepository<TimeTable, Long> {
    Optional<TimeTable> findByUserId(Long userId);
}