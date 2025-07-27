package org.ject.recreation.core.domain.game;

import org.ject.recreation.core.api.controller.request.CreateGameRequest;
import org.ject.recreation.core.api.controller.request.UpdateGameRequest;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
import org.ject.recreation.core.support.error.CoreException;
import org.ject.recreation.storage.db.core.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class GameServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private SessionUserInfoDto sessionUserInfo;
    private UUID gameId;
    private CreateGameRequest createGameRequest;
    private UpdateGameRequest updateGameRequest;
    private List<CreateGameRequest.QuestionRequest> createQuestionRequests = new ArrayList<>();
    private List<UpdateGameRequest.UpdateQuestionRequest> updateQuestionRequests = new ArrayList<>();

    @BeforeEach
    void setUp() {
        UserEntity user = UserEntity.builder()
                .email("test@example.com")
                .platform("kakao")
                .profileImageUrl("https://example.com/profile.png")
                .nickname("Test")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        sessionUserInfo = SessionUserInfoDto.builder()
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .nickname(user.getNickname())
                .build();
        gameId = UUID.randomUUID();

        // CreateGameRequest용 질문 리스트 생성
        for (int i = 0; i < 10; i++) {
            CreateGameRequest.QuestionRequest build = CreateGameRequest.QuestionRequest.builder()
                    .imageUrl("https://example.com/image.png")
                    .questionOrder(i)
                    .questionText("Test " + i)
                    .questionAnswer("Test " + i)
                    .build();
            createQuestionRequests.add(build);
        }

        // UpdateGameRequest용 질문 리스트 생성
        for (int i = 0; i < 10; i++) {
            UpdateGameRequest.UpdateQuestionRequest build = UpdateGameRequest.UpdateQuestionRequest.builder()
                    .imageUrl("https://example.com/image.png")
                    .questionOrder(i)
                    .questionText("Test " + i)
                    .questionAnswer("Test " + i)
                    .version(1)
                    .build();
            updateQuestionRequests.add(build);
        }

        createGameRequest = CreateGameRequest.builder()
                .gameId(gameId)
                .gameTitle("Test Game")
                .gameCreatorEmail(sessionUserInfo.getEmail())
                .gameThumbnailUrl("https://example.com/thumbnail.png")
                .questions(createQuestionRequests)
                .build();

        updateGameRequest = UpdateGameRequest.builder()
                .gameTitle("Test Game")
                .gameCreatorEmail(sessionUserInfo.getEmail())
                .gameThumbnailUrl("https://example.com/thumbnail.png")
                .version(1)
                .questions(updateQuestionRequests)
                .build();

        GameEntity game = GameEntity.builder()
                .gameId(gameId)
                .gameCreator(user)
                .gameTitle("game title")
                .build();

        gameRepository.save(game);
    }

    @Test
    @DisplayName("게임 저장")
    void createGame() {
        String game = gameService.createGame(sessionUserInfo, createGameRequest);
        assertNotNull(game);
    }

    @Nested
    @DisplayName("게임 수정")
    class UpdateGameTest {

        @Test
        @DisplayName("게임 제목 수정")
        void updateGameTitle() {
            // 1. 먼저 게임을 생성
            createGame();

            // 2. 제목만 수정
            updateGameRequest.setGameTitle("Updated Game Title");
            String result = gameService.updateGame(sessionUserInfo, gameId, updateGameRequest);
            
            // 3. 결과 검증
            assertEquals("성공적으로 수정되었습니다.", result);
            
            // 4. DB에서 실제 저장된 데이터 확인
            Optional<GameEntity> gameEntityOpt = gameRepository.findById(gameId);
            assertTrue(gameEntityOpt.isPresent());
            GameEntity gameEntity = gameEntityOpt.get();
            assertEquals("Updated Game Title", gameEntity.getGameTitle());
        }

        @Test
        @DisplayName("게임 썸네일 URL 수정")
        void updateGameThumbnail() {
            // 1. 먼저 게임을 생성
            createGame();

            // 2. 썸네일 URL 수정
            updateGameRequest.setGameThumbnailUrl("https://example.com/new-thumbnail.png");
            String result = gameService.updateGame(sessionUserInfo, gameId, updateGameRequest);
            
            // 3. 결과 검증
            assertEquals("성공적으로 수정되었습니다.", result);
            
            // 4. DB에서 실제 저장된 데이터 확인
            Optional<GameEntity> gameEntityOpt = gameRepository.findById(gameId);
            assertTrue(gameEntityOpt.isPresent());
            GameEntity gameEntity = gameEntityOpt.get();
            assertEquals("https://example.com/new-thumbnail.png", gameEntity.getGameThumbnailUrl());
        }

        @Test
        @DisplayName("게임 문제 수정")
        void updateGameQuestions() {
            // 1. 먼저 게임을 생성
            createGame();

            // 2. 문제 리스트 수정
            updateQuestionRequests.get(0).setQuestionText("0번 질문 수정됨");
            updateQuestionRequests.get(1).setQuestionAnswer("1번 답변 수정됨");
            updateQuestionRequests.get(2).setQuestionOrder(updateQuestionRequests.size() - 1);
            updateQuestionRequests.get(updateQuestionRequests.size() - 1).setQuestionOrder(2);

            updateGameRequest.setQuestions(updateQuestionRequests);
            String result = gameService.updateGame(sessionUserInfo, gameId, updateGameRequest);

            // 3. 결과 검증
            assertEquals("성공적으로 수정되었습니다.", result);

            // 4. DB에서 실제로 저장된 GameEntity를 조회
            Optional<GameEntity> gameEntityOpt = gameRepository.findById(gameId);
            assertTrue(gameEntityOpt.isPresent());
            GameEntity gameEntity = gameEntityOpt.get();

            // 5. 문제 리스트를 order 기준으로 정렬
            List<QuestionEntity> questions = new ArrayList<>(gameEntity.getQuestions());
            questions.sort(Comparator.comparingInt(QuestionEntity::getQuestionOrder));

            // 6. 각 필드가 정상적으로 반영됐는지 검증
            assertEquals("0번 질문 수정됨", questions.get(0).getQuestionText());
            assertEquals("1번 답변 수정됨", questions.get(1).getQuestionAnswer());
            assertEquals(2, questions.get(2).getQuestionOrder());
            assertEquals("Test " + (updateQuestionRequests.size() - 1), questions.get(2).getQuestionText());
        }

        @Test
        @DisplayName("게임 제목과 문제 동시 수정")
        void updateGameTitleAndQuestions() {
            // 1. 먼저 게임을 생성
            createGame();

            // 2. 제목과 문제 동시 수정
            updateGameRequest.setGameTitle("제목과 문제 동시 수정");
            updateQuestionRequests.get(0).setQuestionText("첫 번째 문제 수정");
            updateQuestionRequests.get(1).setQuestionAnswer("두 번째 답변 수정");
            updateGameRequest.setQuestions(updateQuestionRequests);

            String result = gameService.updateGame(sessionUserInfo, gameId, updateGameRequest);

            // 3. 결과 검증
            assertEquals("성공적으로 수정되었습니다.", result);

            // 4. DB에서 실제 저장된 데이터 확인
            Optional<GameEntity> gameEntityOpt = gameRepository.findById(gameId);
            assertTrue(gameEntityOpt.isPresent());
            GameEntity gameEntity = gameEntityOpt.get();
            assertEquals("제목과 문제 동시 수정", gameEntity.getGameTitle());

            // 5. 문제 수정 확인
            List<QuestionEntity> questions = new ArrayList<>(gameEntity.getQuestions());
            questions.sort(Comparator.comparingInt(QuestionEntity::getQuestionOrder));
            assertEquals("첫 번째 문제 수정", questions.get(0).getQuestionText());
            assertEquals("두 번째 답변 수정", questions.get(1).getQuestionAnswer());
        }

        @Test
        @DisplayName("존재하지 않는 게임 수정 시 예외 발생")
        void updateNonExistentGame() {
            UUID nonExistentGameId = UUID.randomUUID();
            
            assertThrows(CoreException.class, () -> {
                gameService.updateGame(sessionUserInfo, nonExistentGameId, updateGameRequest);
            });
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 게임 수정 시 예외 발생")
        void updateGameWithNonExistentUser() {
            // 1. 먼저 게임을 생성
            createGame();

            // 2. 존재하지 않는 사용자 이메일로 수정 시도
            updateGameRequest.setGameCreatorEmail("nonexistent@example.com");
            
            assertThrows(CoreException.class, () -> {
                gameService.updateGame(sessionUserInfo, gameId, updateGameRequest);
            });
        }
    }

    @Test
    @DisplayName("게임 실행")
    void playGame() {
        gameService.playGame(gameId);
        gameService.playGame(gameId);
        gameService.playGame(gameId);
        gameService.playGame(gameId);
        gameService.playGame(gameId);

        Optional<GameEntity> byId = gameRepository.findById(gameId);
        assertEquals(5, byId.get().getPlayCount());
    }
}