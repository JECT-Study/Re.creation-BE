package org.ject.recreation.core.domain.game.question;

public record QuestionResult(
        long questionId,
        int questionOrder,
        String imageUrl,
        String questionText,
        String questionAnswer,
        long version
) { }
