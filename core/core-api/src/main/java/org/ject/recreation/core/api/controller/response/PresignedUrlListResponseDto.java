package org.ject.recreation.core.api.controller.response;

import java.util.List;
import java.util.UUID;

public record PresignedUrlListResponseDto(
        UUID gameId,
        List<PresignedUrlDto> presignedUrls
) {
    public record PresignedUrlDto (
        String imageName,
        int questionOrder,
        String url,
        String key
    ) { }
}
