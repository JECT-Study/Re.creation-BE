package org.ject.recreation.core.api.controller.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GameListResponseDto(
        List<GameDto> games
) {
    public record GameDto(
            UUID gameId,
            String gameThumbnail,
            String gameTitle,
            int questionCount,
            long playCount,
            LocalDateTime updatedAt
    ) { }
}
