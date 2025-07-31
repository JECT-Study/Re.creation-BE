package org.ject.recreation.core.domain.game;

import org.ject.recreation.S3PresignedUrl;
import org.ject.recreation.S3PresignedUrlManager;
import org.ject.recreation.core.domain.game.upload.PresignedUrlListResult;
import org.ject.recreation.core.domain.game.upload.PresignedUrlQuery;
import org.ject.recreation.core.domain.game.upload.PresignedUrlResult;
import org.ject.recreation.core.api.controller.request.CreateGameRequest;
import org.ject.recreation.core.api.controller.request.UpdateGameRequest;
import org.ject.recreation.core.api.controller.response.GameListResponseDto;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
import org.ject.recreation.core.domain.game.question.Question;
import org.ject.recreation.core.domain.game.question.QuestionReader;
import org.ject.recreation.core.domain.game.question.QuestionResult;
import org.ject.recreation.core.support.error.CoreException;
import org.ject.recreation.core.support.error.ErrorType;
import org.ject.recreation.storage.db.core.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameReader gameReader;
    private final QuestionReader questionReader;
    private final S3PresignedUrlManager s3PresignedUrlManager;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final QuestionRepository questionRepository;

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

    @Transactional
    public String createGame(SessionUserInfoDto userInfo,
                             CreateGameRequest createGameRequest) {
        // 사용자 정보 조회
        UserEntity user = userRepository.findById(userInfo.getEmail())
                .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED));

        GameEntity gameEntity = createGameRequest.toGameEntity(user);
        gameRepository.save(gameEntity);
        return "성공적으로 저장되었습니다.";
    }

    @Transactional
    public String updateGame(SessionUserInfoDto userInfo, UUID gameId, UpdateGameRequest updateGameRequest) {
        // 사용자 정보 조회
        UserEntity existingUser = userRepository.findById(userInfo.getEmail())
                .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED));

        UserEntity newUser = userRepository.findById(updateGameRequest.getGameCreatorEmail())
                .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED));

        // TODO
        // 로그인 user <-> request user
        // 다중 편집 <- 권한?

        // 기존 게임 엔티티 조회
        GameEntity existingGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new CoreException(ErrorType.GAME_NOT_FOUND));

        // 기존 질문들 삭제
        questionRepository.deleteByGame(existingGame);

        GameEntity game = updateGameRequest.fromGameEntity(existingUser, existingGame);

        gameRepository.save(game);
        return "성공적으로 수정되었습니다.";
    }

    @Transactional
    public String playGame(UUID gameId) {
        GameEntity existingGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new CoreException(ErrorType.GAME_NOT_FOUND));
        existingGame.plusCount();
        return "성공적으로 실행되었습니다.";
    }

    @Transactional(readOnly = true)
    public GameListResponseDto getDefaultGame(){
        // 기본 게임들을 조회 (SampleDataInitializer에서 생성한 게임들)
        List<GameEntity> defaultGames = gameRepository.
                findAllByGameCreatorEmailAndIsDeletedFalse("jectreation518@gmail.com");
        
        // GameListResponseDto로 변환
        List<GameListResponseDto.GameDto> gameDtos = defaultGames.stream()
                .map(game -> GameListResponseDto.GameDto.builder()
                        .gameId(game.getGameId())
                        .gameThumbnail(game.getGameThumbnailUrl())
                        .gameTitle(game.getGameTitle())
                        .questionCount(game.getQuestionCount())
                        .playCount(game.getPlayCount())
                        .updatedAt(game.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return GameListResponseDto.builder()
                .games(gameDtos)
                .build();
    }
  
}
