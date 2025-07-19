package org.ject.recreation.core.api.controller.request;

import org.ject.recreation.core.domain.game.GameListQuery;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameListRequestDto(
        UUID cursorGameId,
        Long cursorPlayCount,
        LocalDateTime cursorUpdatedAt,
        int limit,
        String query
) {
    public GameListQuery toGameListQuery() {
        return new GameListQuery(
                cursorGameId,
                cursorPlayCount,
                cursorUpdatedAt,
                limit,
                query
        );
    }
}
