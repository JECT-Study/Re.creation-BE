package org.ject.recreation.storage.db.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OptimisticLock;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name="game")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameEntity extends BaseEntity {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID gameId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "game_creator_email",
            referencedColumnName = "email",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            nullable = false
    )
    private UserEntity gameCreator;

    @Column(nullable = false, length = 200)
    private String gameTitle;

    @Column(nullable = true, length = 255)
    private String gameThumbnailUrl;

    @Column(nullable = false)
    @OptimisticLock(excluded = true)
    @Builder.Default
    private boolean isShared = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(nullable = false)
    @Builder.Default
    private int questionCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private long playCount = 0;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private long version = 1;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    // 1:N 관계 - 게임에 포함된 문제들
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuestionEntity> questions = new ArrayList<>();

    public GameEntity update(GameEntity gameEntity) {
        this.gameTitle = gameEntity.gameTitle;
        this.gameThumbnailUrl = gameEntity.gameThumbnailUrl;
        this.questionCount = gameEntity.questionCount;
        this.playCount = gameEntity.playCount;
        this.version = gameEntity.version;
        this.deletedAt = gameEntity.deletedAt;
        this.gameCreator = gameEntity.gameCreator;
        return this;
    }

    public void plusCount(){
        this.playCount += 1;
    }

    public void addQuestion(QuestionEntity question) {
        this.questions.add(question);
        question.setGame(this);
    }

    public void softDelete() {
        if (this.isDeleted) {
            return;
        }
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void share() {
        this.isShared = true;
    }

    public void unShare() {
        this.isShared = false;
    }
}