package org.ject.recreation.core.api.controller.response;

import java.util.UUID;

public record GameListItemResponse(
        UUID gameId,
        String gameThumbnail,
        String gameTitle,
        int questionCount,
        long playCount,
        String updatedAt
) { }
