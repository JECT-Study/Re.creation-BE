package org.ject.recreation.core.domain.game.upload;

public record PresignedUrlResult(
        String imageName,
        int questionOrder,
        String url,
        String key
) { }
