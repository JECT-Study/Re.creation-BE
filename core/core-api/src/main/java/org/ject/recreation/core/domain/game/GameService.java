package org.ject.recreation.core.domain.game;

import org.ject.recreation.core.domain.game.question.Question;
import org.ject.recreation.core.domain.game.question.QuestionReader;
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
        List<GameListItem> gameListItems = gameReader.getGameList(
                gameListQuery.toGameListCursor(),
                gameListQuery.limit(),
                gameListQuery.query());

        return new GameListResult(gameListItems);
    }

    public GameDetailResult getGameDetail(UUID gameId) {
        Game game = gameReader.getGameByGameId(gameId);
        List<Question> questions = questionReader.getQuestionsByGameId(gameId);

        return new GameDetailResult(
                game.gameTitle(),
                "test_nickname", // TODO: 실제 닉네임을 사용자 정보로부터 가져와야 함
                game.questionCount(),
                game.version(),
                questions
        );
    }
}
