package org.ject.recreation.core.domain.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MyGameListResult(
        List<MyGameResult> games
) {
    public record MyGameResult(
            UUID gameId,
            String gameThumbnailUrl,
            String gameTitle,
            int questionCount,
            boolean isShared,
            long playCount,
            LocalDateTime updatedAt
    ) { }
}
