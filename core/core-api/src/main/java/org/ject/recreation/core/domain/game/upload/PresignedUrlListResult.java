package org.ject.recreation.core.domain.game.upload;

import java.util.List;
import java.util.UUID;

public record PresignedUrlListResult(
        UUID gameId,
        List<PresignedUrlResult> presignedUrls
) {
}
