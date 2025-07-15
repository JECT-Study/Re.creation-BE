package org.ject.recreation.core.api.controller.response;

import java.util.List;

public record GameListResponseDto(List<GameListItemResponse> games) {
}
