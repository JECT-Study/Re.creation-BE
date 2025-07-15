package org.ject.recreation.core.domain.game;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameListQuery(
        UUID cursorGameId,
        Long cursorPlayCount,
        LocalDateTime cursorUpdatedAt,
        int limit,
        String query
) {
    public GameListCursor toGameListCursor() {
        return new GameListCursor(
                cursorGameId,
                cursorPlayCount,
                cursorUpdatedAt
        );
    }
}
