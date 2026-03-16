package com.speakview.speakview.domain.user.repository;

import com.speakview.speakview.domain.user.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
