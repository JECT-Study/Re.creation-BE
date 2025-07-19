package org.ject.recreation.storage.db.core;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name="game")
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
    private boolean isShared = false;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false)
    private int questionCount = 0;

    @Column(nullable = false)
    private long playCount = 0;

    @Version
    @Column(nullable = false)
    private long version = 1;

    @Column(nullable = true)
    private LocalDateTime deletedAt;
}