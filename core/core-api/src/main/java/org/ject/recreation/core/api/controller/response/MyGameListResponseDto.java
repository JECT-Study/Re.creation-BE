package org.ject.recreation.core.api.controller.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MyGameListResponseDto(
        List<MyGameDto> games
) {
    public record MyGameDto(
            UUID gameId,
            String gameThumbnailUrl,
            String gameTitle,
            int questionCount,
            boolean isShared,
            long playCount,
            LocalDateTime updatedAt
    ) { }
}
