package org.ject.recreation.core.domain.game;

import lombok.RequiredArgsConstructor;
import org.ject.recreation.core.api.controller.request.CreateGameRequest;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
import org.ject.recreation.core.domain.game.question.Question;
import org.ject.recreation.core.domain.game.question.QuestionReader;
import org.ject.recreation.core.domain.game.question.QuestionResult;
import org.ject.recreation.core.support.error.CoreException;
import org.ject.recreation.core.support.error.ErrorType;
import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.GameRepository;
import org.ject.recreation.storage.db.core.QuestionEntity;
import org.ject.recreation.storage.db.core.QuestionRepository;
import org.ject.recreation.storage.db.core.User;
import org.ject.recreation.storage.db.core.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameReader gameReader;
    private final QuestionReader questionReader;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final QuestionRepository questionRepository;

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

    @Transactional
    public String createGame(SessionUserInfoDto userInfo,
                             CreateGameRequest createGameRequest){
        // 사용자 정보 조회
        User user = userRepository.findById(userInfo.getEmail())
                .orElseThrow(()-> new CoreException(ErrorType.UNAUTHORIZED));
        
        // 기존 게임 엔티티 조회
        GameEntity existingGame = gameRepository.findById(createGameRequest.getGameId())
                .orElseThrow(() -> new CoreException(ErrorType.GAME_NOT_FOUND));
        
        // 기존 질문들 삭제
        questionRepository.deleteByGame(existingGame);
        
        // 새로운 질문들 생성
        List<QuestionEntity> questionEntities = createGameRequest.getQuestions().stream()
                .map(questionRequest -> QuestionEntity.builder()
                        .game(existingGame)
                        .questionOrder(questionRequest.getQuestionOrder())
                        .questionText(questionRequest.getQuestionText())
                        .questionAnswer(questionRequest.getQuestionAnswer())
                        .imageUrl(questionRequest.getImageUrl())
                        .build())
                .toList();
        
        // 새로운 게임 엔티티 생성 (기존 엔티티의 ID와 기본값들을 유지하면서 업데이트)
        GameEntity updatedGame = GameEntity.builder()
                .gameId(createGameRequest.getGameId())
                .gameTitle(createGameRequest.getGameTitle())
                .gameCreatorEmail(createGameRequest.getGameCreatorEmail())
                .gameThumbnailUrl(createGameRequest.getGameThumbnailUrl())
                .questionCount(createGameRequest.getQuestions().size())
                .playCount(existingGame.getPlayCount())
                .isShared(existingGame.isShared())
                .isDeleted(existingGame.isDeleted())
                .version(existingGame.getVersion())
                .deletedAt(existingGame.getDeletedAt())
                .user(user)
                .questions(questionEntities)
                .build();
        
        // 질문들에 게임 참조 설정
        questionEntities.forEach(question -> question.setGame(updatedGame));
        
        // 저장
        GameEntity savedGame = gameRepository.save(updatedGame);
        questionRepository.saveAll(questionEntities);
        
        return "성공적으로 저장되었습니다.";
    }

    @Transactional
    public String updateGame(SessionUserInfoDto userInfo, UUID gameId, CreateGameRequest createGameRequest) {
        // 사용자 정보 조회
        User user = userRepository.findById(userInfo.getEmail())
                .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED));

        // 기존 게임 엔티티 조회
        GameEntity existingGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new CoreException(ErrorType.GAME_NOT_FOUND));

        // 기존 질문들 삭제
        questionRepository.deleteByGame(existingGame);

        // 새로운 질문들 생성
        List<QuestionEntity> questionEntities = createGameRequest.getQuestions().stream()
                .map(questionRequest -> QuestionEntity.builder()
                        .game(existingGame)
                        .questionOrder(questionRequest.getQuestionOrder())
                        .questionText(questionRequest.getQuestionText())
                        .questionAnswer(questionRequest.getQuestionAnswer())
                        .imageUrl(questionRequest.getImageUrl())
                        .build())
                .toList();

        // 기존 게임 엔티티 정보 업데이트
        existingGame.setGameTitle(createGameRequest.getGameTitle());
        existingGame.setGameThumbnailUrl(createGameRequest.getGameThumbnailUrl());
        existingGame.setQuestionCount(createGameRequest.getQuestions().size());
        existingGame.setUser(user);
        existingGame.setQuestions(questionEntities);

        // 질문들에 게임 참조 설정
        questionEntities.forEach(question -> question.setGame(existingGame));

        // 저장
        GameEntity savedGame = gameRepository.save(existingGame);
        questionRepository.saveAll(questionEntities);

        return "성공적으로 수정되었습니다.";
    }

    public String playGame(UUID gameId){
        GameEntity existingGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new CoreException(ErrorType.GAME_NOT_FOUND));
        existingGame.plusCount();
        return "성공적으로 실행되었습니다.";
    }
}
