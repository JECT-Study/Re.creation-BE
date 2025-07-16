package org.ject.recreation.core.domain.game.question;

import org.ject.recreation.storage.db.core.QuestionEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public record Question(
        long questionId,
        UUID gameId,
        int questionOrder,
        String questionText,
        String questionAnswer,
        String imageUrl,
        long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Question from(QuestionEntity question) {
        return new Question(
                question.getQuestionId(),
                question.getGameId(),
                question.getQuestionOrder(),
                question.getQuestionText(),
                question.getQuestionAnswer(),
                question.getImageUrl(),
                question.getVersion(),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
