package com.speakview.speakview.domain.user.repository;

import com.speakview.speakview.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
