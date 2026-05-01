package com.speakview.speakview.domain.user.entity;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.speakview.speakview.domain.ai.entity.Content;
import com.speakview.speakview.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "Subject",
        indexes = {
                @Index(name = "idx_subject_timetable", columnList = "timetable_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TimeTable timetable;

    @Column(name = "name")
    private String name;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Builder.Default
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Content> contents = new ArrayList<>();
}