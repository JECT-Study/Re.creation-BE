package org.ject.recreation.core.domain.game;

import org.ject.recreation.S3PresignedUrl;
import org.ject.recreation.S3PresignedUrlManager;
import org.ject.recreation.core.domain.game.question.Question;
import org.ject.recreation.core.domain.game.question.QuestionReader;
import org.ject.recreation.core.domain.game.question.QuestionResult;
import org.ject.recreation.core.domain.game.upload.PresignedUrlListResult;
import org.ject.recreation.core.domain.game.upload.PresignedUrlQuery;
import org.ject.recreation.core.domain.game.upload.PresignedUrlResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class GameService {

    private final GameReader gameReader;
    private final QuestionReader questionReader;
    private final S3PresignedUrlManager s3PresignedUrlManager;

    public GameService(GameReader gameReader,
                       QuestionReader questionReader,
                       S3PresignedUrlManager s3PresignedUrlManager) {
        this.gameReader = gameReader;
        this.questionReader = questionReader;
        this.s3PresignedUrlManager = s3PresignedUrlManager;
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

    public PresignedUrlListResult getPresignedUrls(PresignedUrlQuery presignedUrlQuery) {
        return generatePresignedUrls(UUID.randomUUID(), presignedUrlQuery);
    }

    public PresignedUrlListResult getPresignedUrls(UUID gameId, PresignedUrlQuery presignedUrlQuery) {
        Game game = gameReader.getGameByGameId(gameId);
        // TODO: 게임 권한 소지 여부 확인 로직 추가

        return generatePresignedUrls(gameId, presignedUrlQuery);
    }

    private PresignedUrlListResult generatePresignedUrls(UUID gameId, PresignedUrlQuery presignedUrlQuery) {
        List<S3PresignedUrl> presignedUrls = presignedUrlQuery.queries().stream()
                .map(query -> {
                    String key = String.format("games/%s/%s", gameId, query.imageName());
                    return s3PresignedUrlManager.generatePresignedUrl(key);
                })
                .toList();

        return new PresignedUrlListResult(
                gameId,
                IntStream.range(0, presignedUrlQuery.queries().size())
                        .mapToObj(i -> {
                            PresignedUrlQuery.QuestionImageQuery query = presignedUrlQuery.queries().get(i);
                            S3PresignedUrl presignedUrl = presignedUrls.get(i);
                            return new PresignedUrlResult(
                                    query.imageName(),
                                    query.questionOrder(),
                                    presignedUrl.url(),
                                    presignedUrl.key());
                        }).toList()
        );
    }

}
