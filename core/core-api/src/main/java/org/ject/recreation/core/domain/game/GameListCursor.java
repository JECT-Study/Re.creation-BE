package org.ject.recreation.core.domain.game;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameListCursor(
        UUID cursorGameId,
        Long cursorPlayCount,
        LocalDateTime cursorUpdatedAt
) {
}
