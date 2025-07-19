package org.ject.recreation.core.domain.game;

import org.ject.recreation.core.domain.game.question.Question;
import org.ject.recreation.core.domain.game.question.QuestionReader;
import org.ject.recreation.core.domain.game.question.QuestionResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    private final GameReader gameReader;
    private final QuestionReader questionReader;

    public GameService(GameReader gameReader,
                       QuestionReader questionReader) {
        this.gameReader = gameReader;
        this.questionReader = questionReader;
    }

    @Transactional(readOnly = true)
    public GameListResult getGameList(GameListQuery gameListQuery) {
        List<Game> games = gameReader.getGameList(
                gameListQuery.toGameListCursor(),
                gameListQuery.limit(),
                gameListQuery.query());

        return new GameListResult(games.stream()
                .map(game -> new GameResult(
                        game.gameId(),
                        game.gameThumbnailUrl(),
                        game.gameTitle(),
                        game.questionCount(),
                        game.playCount(),
                        game.updatedAt()))
                .toList());
    }

    @Transactional(readOnly = true)
    public GameDetailResult getGameDetail(UUID gameId) {
        Game game = gameReader.getGameByGameId(gameId);
        List<Question> questions = questionReader.getQuestionsByGameId(gameId);

        return new GameDetailResult(
                game.gameTitle(),
                game.nickname(),
                game.questionCount(),
                game.version(),
                questions.stream()
                        .map(question -> new QuestionResult(
                                question.questionId(),
                                question.questionOrder(),
                                question.imageUrl(),
                                question.questionText(),
                                question.questionAnswer(),
                                question.version()))
                        .toList()
        );
    }
}
