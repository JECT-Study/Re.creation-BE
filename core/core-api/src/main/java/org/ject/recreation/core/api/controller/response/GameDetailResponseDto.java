package org.ject.recreation.core.api.controller.response;

import java.util.List;

public record GameDetailResponseDto(
        String gameTitle,
        String nickname,
        int questionCount,
        long version,
        List<QuestionDto> questions
) {
    public record QuestionDto(
            long questionId,
            int questionOrder,
            String imageUrl,
            String questionText,
            String questionAnswer,
            long version
    ) { }
}
