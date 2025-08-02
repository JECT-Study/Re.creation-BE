package org.ject.recreation.core.domain.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record MyGameListQuery(
        UUID cursorGameId,
        LocalDateTime cursorUpdatedAt,
        int limit
) {
    public MyGameListCursor toMyGameListCursor() {
        return new MyGameListCursor(
                cursorGameId,
                cursorUpdatedAt
        );
    }
}
