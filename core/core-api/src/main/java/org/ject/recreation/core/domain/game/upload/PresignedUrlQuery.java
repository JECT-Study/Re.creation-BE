package org.ject.recreation.core.domain.game.upload;

import java.util.List;

public record PresignedUrlQuery(
        List<QuestionImageQuery> queries
) {
    public record QuestionImageQuery (
        String imageName,
        int questionOrder
    ) { }
}
