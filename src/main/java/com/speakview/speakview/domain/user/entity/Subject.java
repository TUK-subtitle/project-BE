package com.speakview.speakview.domain.user.entity;

import com.speakview.speakview.domain.ai.entity.Content;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "Subject",
        indexes = {
                @Index(name = "idx_subject_timetable", columnList = "timeTableId")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeTableId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TimeTable timetable;

    @Column(nullable = false, length = 20)
    private String startTime;

    @Column(nullable = false)
    private Integer progressTime;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Content> contents = new ArrayList<>();
}