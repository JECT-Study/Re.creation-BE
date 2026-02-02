package org.ject.recreation.core.domain.game;

import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record Game(
        UUID gameId,
        String creatorEmail,
        String creatorNickname,
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
        String nickname =
                game.getGameCreator().getRole() == UserRole.ADMIN
                        ? "게임마스터"
                        : game.getGameCreator().getNickname();

        return new Game(
                game.getGameId(),
                game.getGameCreator().getEmail(),
                nickname,
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
