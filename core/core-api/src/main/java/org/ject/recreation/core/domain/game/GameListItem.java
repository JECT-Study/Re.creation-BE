package org.ject.recreation.core.domain.game;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameListItem(
        UUID gameId,
        String gameThumbnail,
        String gameTitle,
        int questionCount,
        long playCount,
        LocalDateTime updatedAt
) {
}
