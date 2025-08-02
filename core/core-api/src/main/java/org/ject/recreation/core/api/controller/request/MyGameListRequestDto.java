package org.ject.recreation.core.api.controller.request;

import org.ject.recreation.core.domain.user.MyGameListQuery;

import java.time.LocalDateTime;
import java.util.UUID;

public record MyGameListRequestDto(
        UUID cursorGameId,
        LocalDateTime cursorUpdatedAt,
        int limit
) {
    public MyGameListQuery toMyGameListQuery() {
        return new MyGameListQuery(
                cursorGameId,
                cursorUpdatedAt,
                limit
        );
    }
}
