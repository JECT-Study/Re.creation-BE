package org.ject.recreation.core.domain.game.question;

import org.ject.recreation.storage.db.core.QuestionEntity;
import org.ject.recreation.storage.db.core.QuestionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class QuestionReader {
    private final QuestionRepository questionRepository;

    public QuestionReader(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> getQuestionsByGameId(UUID gameId) {
        List<QuestionEntity> questions = questionRepository.findByGameId(gameId);
        return questions.stream()
                .map(Question::from)
                .toList();
    }
}
