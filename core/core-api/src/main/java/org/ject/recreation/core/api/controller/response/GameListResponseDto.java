package org.ject.recreation.core.api.controller.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record GameListResponseDto(
        List<GameDto> games
) {
    @Builder
    public record GameDto(
            UUID gameId,
            String gameThumbnailUrl,
            String gameTitle,
            int questionCount,
            long playCount,
            LocalDateTime updatedAt
    ) { }
}
