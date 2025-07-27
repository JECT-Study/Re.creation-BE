package org.ject.recreation.core.domain.game;

import lombok.RequiredArgsConstructor;
import org.ject.recreation.core.api.controller.request.CreateGameRequest;
import org.ject.recreation.core.api.controller.request.UpdateGameRequest;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameReader gameReader;
    private final QuestionReader questionReader;
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

    public String playGame(UUID gameId) {
        GameEntity existingGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new CoreException(ErrorType.GAME_NOT_FOUND));
        existingGame.plusCount();
        return "성공적으로 실행되었습니다.";
    }
}
