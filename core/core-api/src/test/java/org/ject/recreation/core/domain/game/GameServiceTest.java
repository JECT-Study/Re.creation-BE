package org.ject.recreation.core.domain.game;

import org.ject.recreation.core.api.controller.request.CreateGameRequest;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
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
    private CreateGameRequest gameRequest;
    private List<CreateGameRequest.QuestionRequest> questionRequests = new ArrayList<>();

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

        for (int i = 0; i < 10; i++) {
            CreateGameRequest.QuestionRequest build = CreateGameRequest.QuestionRequest.builder()
                    .imageUrl("https://example.com/image.png")
                    .questionOrder(i)
                    .questionText("Test " + i)
                    .questionAnswer("Test " + i)
                    .build();
            questionRequests.add(build);
        }

        gameRequest = CreateGameRequest.builder()
                .gameId(gameId)
                .gameTitle("Test Game")
                .gameCreatorEmail(sessionUserInfo.getEmail())
                .gameThumbnailUrl("https://example.com/thumbnail.png")
                .questions(questionRequests)
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
        String game = gameService.createGame(sessionUserInfo, gameRequest);
        assertNotNull(game);
    }

    @Nested
    @DisplayName("게임 수정")
    class updateGame{

        @Test
        @DisplayName("제목 수정")
        void updateTitle() {
            createGame();
            gameRequest.setGameTitle("game title update");
            gameService.updateGame(sessionUserInfo, gameId,gameRequest);
            Optional<GameEntity> gameEntity = gameRepository.findById(gameId);
            assertEquals("game title update", gameEntity.get().getGameTitle());
        }

        @Test
        @DisplayName("문제 수정")
        void updateQuestion() {
            // 1. 먼저 게임을 생성
            createGame();

            // 2. 문제 리스트 일부 수정
            questionRequests.get(0).setQuestionText("0번 질문 바뀜");
            questionRequests.get(1).setQuestionAnswer("1번 답변 바뀜");
            questionRequests.get(2).setQuestionOrder(questionRequests.size() - 1);
            questionRequests.get(questionRequests.size() - 1).setQuestionOrder(2);

            // 3. 수정된 문제 리스트로 게임 수정 요청
            gameRequest.setQuestions(questionRequests);
            gameService.updateGame(sessionUserInfo, gameId, gameRequest);

            // 4. DB에서 실제로 저장된 GameEntity를 조회
            Optional<GameEntity> gameEntityOpt = gameRepository.findById(gameId);
            assertTrue(gameEntityOpt.isPresent());
            GameEntity gameEntity = gameEntityOpt.get();

            // 5. 문제 리스트를 order 기준으로 정렬
            List<QuestionEntity> questions = new ArrayList<>(gameEntity.getQuestions());
            questions.sort(Comparator.comparingInt(QuestionEntity::getQuestionOrder));

            // 6. 각 필드가 정상적으로 반영됐는지 검증
            assertEquals("0번 질문 바뀜", questions.get(0).getQuestionText());
            assertEquals("1번 답변 바뀜", questions.get(1).getQuestionAnswer());
            assertEquals(2, questions.get(2).getQuestionOrder());
            assertEquals("Test " + (questionRequests.size() - 1), questions.get(2).getQuestionText());
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