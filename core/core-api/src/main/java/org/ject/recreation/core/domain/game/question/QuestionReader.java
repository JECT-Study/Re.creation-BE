package org.ject.recreation.core.domain.game.question;

import lombok.RequiredArgsConstructor;
import org.ject.recreation.core.support.error.CoreException;
import org.ject.recreation.core.support.error.ErrorType;
import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.GameRepository;
import org.ject.recreation.storage.db.core.QuestionEntity;
import org.ject.recreation.storage.db.core.QuestionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class QuestionReader {
    private final QuestionRepository questionRepository;
    private final GameRepository gameRepository;

    public List<Question> getQuestionsByGameId(UUID gameId) {
        GameEntity existingGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new CoreException(ErrorType.GAME_NOT_FOUND));

        List<QuestionEntity> questions = questionRepository.findByGameOrderByQuestionOrder(existingGame);
        return questions.stream()
                .map(Question::from)
                .toList();
    }
}
