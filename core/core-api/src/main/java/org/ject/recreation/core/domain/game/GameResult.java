package org.ject.recreation.core.domain.game;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameResult(
        UUID gameId,
        String gameThumbnailUrl,
        String gameTitle,
        int questionCount,
        long playCount,
        LocalDateTime updatedAt
) {
}
