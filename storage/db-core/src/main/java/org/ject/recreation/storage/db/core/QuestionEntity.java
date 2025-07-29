package org.ject.recreation.storage.db.core;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name="question")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long questionId;

//    @Column(nullable = false, columnDefinition = "BINARY(16)")
//    private UUID gameId;

    @Column(nullable = false)
    private int questionOrder;

    @Column(nullable = false, length = 60)
    private String questionText;

    @Column(nullable = false, length = 60)
    private String questionAnswer;

    @Column(nullable = true, length = 1000)
    private String imageUrl;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private long version = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;
}
