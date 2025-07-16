package org.ject.recreation.core.domain.game;

import org.ject.recreation.storage.db.core.GameEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 게임 정보를 담는 클래스
 *
 * <p> Implement Layer 에서 사용됩니다
 */
public record Game(
        UUID gameId,
        String gameCreatorEmail,
        String gameTitle,
        String gameThumbnailUrl,
        boolean isShared,
        boolean isDeleted,
        int questionCount,
        long playCount,
        long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
    public static Game from(GameEntity game) {
        return new Game(
                game.getGameId(),
                game.getGameCreatorEmail(),
                game.getGameTitle(),
                game.getGameThumbnailUrl(),
                game.isShared(),
                game.isDeleted(),
                game.getQuestionCount(),
                game.getPlayCount(),
                game.getVersion(),
                game.getCreatedAt(),
                game.getUpdatedAt(),
                game.getDeletedAt()
        );
    }
}
