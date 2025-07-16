package org.ject.recreation.core.domain.game;

import org.ject.recreation.core.domain.game.question.Question;
import org.ject.recreation.core.domain.game.question.QuestionReader;
import org.ject.recreation.core.domain.game.question.QuestionResult;
import org.springframework.stereotype.Service;

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

    public GameDetailResult getGameDetail(UUID gameId) {
        Game game = gameReader.getGameByGameId(gameId);
        List<Question> questions = questionReader.getQuestionsByGameId(gameId);

        String gameCreatorEmail = game.gameCreatorEmail();
        String gameCreatorNickname = "test_nickname"; // TODO: 실제 닉네임을 사용자 정보로부터 가져와야 함

        return new GameDetailResult(
                game.gameTitle(),
                gameCreatorNickname,
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
