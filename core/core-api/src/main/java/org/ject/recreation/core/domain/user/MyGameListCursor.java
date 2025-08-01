package org.ject.recreation.core.domain.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record MyGameListCursor(
        UUID cursorGameId,
        LocalDateTime cursorUpdatedAt
) { }
