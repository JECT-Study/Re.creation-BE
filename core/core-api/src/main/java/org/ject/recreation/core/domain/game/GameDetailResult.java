package org.ject.recreation.core.domain.game;

import org.ject.recreation.core.domain.game.question.QuestionResult;

import java.util.List;

public record GameDetailResult(
        String gameTitle,
        String nickname,
        int questionCount,
        long version,
        List<QuestionResult> questions
) { }
