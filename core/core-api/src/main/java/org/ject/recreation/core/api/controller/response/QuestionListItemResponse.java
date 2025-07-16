package org.ject.recreation.core.api.controller.response;

public record QuestionListItemResponse(
        long questionId,
        int questionOrder,
        String imageUrl,
        String questionText,
        String questionAnswer,
        long version
) { }
