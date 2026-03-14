package com.speakview.speakview.domain.memo.entity;

import com.speakview.speakview.domain.ai.entity.Content;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "Memo",
        indexes = {
                @Index(name="idx_memo_content", columnList="contentId")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contentId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Content content;

    @Lob
    @Column(nullable = false)
    private String memoText;

    @Column(nullable = false, length = 20)
    private Integer timestamp;
}