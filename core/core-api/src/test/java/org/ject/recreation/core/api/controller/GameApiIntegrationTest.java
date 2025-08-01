package org.ject.recreation.core.api.controller;

import org.ject.recreation.core.api.controller.request.PresignedUrlListRequestDto;
import org.ject.recreation.core.api.controller.response.GameDetailResponseDto;
import org.ject.recreation.core.api.controller.response.GameListResponseDto;
import org.ject.recreation.core.api.controller.response.PresignedUrlListResponseDto;
import org.ject.recreation.core.support.response.ApiResponse;
import org.ject.recreation.storage.db.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local") // H2 사용하는 프로필
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    private HttpHeaders headers;

    private UserEntity user;

    private List<GameEntity> games;

    private List<QuestionEntity> questions;

    @BeforeEach
    void setUp() {
        ResponseEntity<String> loginResponse = restTemplate.exchange(
                "/test/login/kakao",
                HttpMethod.POST,
                null,
                String.class
        );

        String sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(sessionCookie).contains("JSESSIONID");

        this.headers = new HttpHeaders();
        this.headers.add(HttpHeaders.COOKIE, sessionCookie);
        this.headers.setContentType(MediaType.APPLICATION_JSON);

        user = new UserEntity(
                "test@example.com",
                "kakao",
                "http://image.url/question",
                "테스트유저",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        userRepository.save(user);

        games = List.of(
                createGame("가장 인기 퀴즈", user, 400,true, false),
                createGame("OX 퀴즈", user, 310, true, false),
                createGame("O/X 퀴즈", user, 300, true, false),
                createGame("인물 퀴즈 2", user, 250, true, false),
                createGame("최근 업데이트 퀴즈", user, 210, true, false),
                createGame("영화 명대사 퀴즈  1", user, 200, true, false),
                createGame("영화 명대사 퀴즈  2 (비공유)", user, 200, false, false),
                createGame("연상 퀴즈", user, 180, true, false),
                createGame("지구과학 퀴즈 1 (삭제)", user, 150, true, true),
                createGame("지구과학 퀴즈 2 (삭제)", user, 150, true, true),
                createGame("한국사 퀴즈", user, 120, true, false),
                createGame("지구과학 퀴즈 3 (비공유)", user, 110, false, false),
                createGame("인물 퀴즈 1 (삭제)", user, 100, true, true),
                createGame("인물 퀴즈 2 (비공유 삭제)", user, 95, false, true),
                createGame("수학 퀴즈", user, 90, true, false),
                createGame("세계사 퀴즈", user, 80,  true, false),
                createGame("영어 퀴즈", user, 70, true, false),
                createGame("과학 퀴즈", user, 60, true, false),
                createGame("영화 명대사 퀴즈 3", user, 50, true, false),
                createGame("랜덤 퀴즈", user, 50, true, false)
        );
        gameRepository.saveAll(games);

        questions = List.of(
                createQuestion(games.getFirst(), 1, "가장 인기 퀴즈 질문 1", "답변 1"),
                createQuestion(games.getFirst(), 2, "가장 인기 퀴즈 질문 2", "답변 2"),
                createQuestion(games.getFirst(), 3, "가장 인기 퀴즈 질문 3", "답변 3"),
                createQuestion(games.getFirst(), 4, "가장 인기 퀴즈 질문 4", "답변 4"),
                createQuestion(games.getFirst(), 5, "가장 인기 퀴즈 질문 5", "답변 5"),
                createQuestion(games.getFirst(), 6, "가장 인기 퀴즈 질문 6", "답변 6")
        );
        questionRepository.saveAll(questions);
    }

    @Test
    void 게임_목록_조회시_삭제되지않고_공유된_게임만_반환된다() {
        // when
        ResponseEntity<ApiResponse<GameListResponseDto>> response = restTemplate.exchange(
                "/games?limit=100",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        GameListResponseDto gameListResponse = (GameListResponseDto) response.getBody().getData();
        List<GameListResponseDto.GameDto> games = gameListResponse.games();

        assertThat(games.stream().anyMatch(game -> game.gameTitle().contains("비공유"))).isFalse();
        assertThat(games.stream().anyMatch(game -> game.gameTitle().contains("삭제"))).isFalse();
    }

    @Test
    void 게임_목록_조회시_게임플레이_최근수정일시_게임식별자_순으로_내림차순_정렬되어_반환된다() {
        // when
        ResponseEntity<ApiResponse<GameListResponseDto>> response = restTemplate.exchange(
                "/games?limit=100",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        GameListResponseDto gameListResponse = (GameListResponseDto) response.getBody().getData();
        List<GameListResponseDto.GameDto> games = gameListResponse.games();

        AtomicBoolean isSorted = new AtomicBoolean(true);

        IntStream.range(0, games.size() - 1).forEach(i -> {
            GameListResponseDto.GameDto current = games.get(i);
            GameListResponseDto.GameDto next = games.get(i + 1);

            if (current.playCount() > next.playCount()
                    || current.updatedAt().isAfter(next.updatedAt())
                    || current.gameId().compareTo(next.gameId()) > 0) {
                return;
            }

            isSorted.set(false);
        });

        assertThat(isSorted.get()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 5, 7, 9, 11})
    void limit_파라미터에_맞춘_개수의_게임만_반환한다(int limit) {
        // when
        ResponseEntity<ApiResponse<GameListResponseDto>> response = restTemplate.exchange(
                "/games?limit=" + limit,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        GameListResponseDto gameListResponse = (GameListResponseDto) response.getBody().getData();
        List<GameListResponseDto.GameDto> games = gameListResponse.games();

        assertThat(games).hasSize(limit);
    }

    @Test
    void query_파라미터로_게임명을_포함하는_게임만_조회한다() {
        // when
        ResponseEntity<ApiResponse<GameListResponseDto>> response = restTemplate.exchange(
                "/games?limit=100&query=영화",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        GameListResponseDto gameListResponse = (GameListResponseDto) response.getBody().getData();
        List<GameListResponseDto.GameDto> games = gameListResponse.games();

        assertThat(games).isNotEmpty();
        assertThat(games).allMatch(game -> game.gameTitle().contains("영화"));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 7, 10})
    void 커서기반_페이징_정상작동하고_중복없이_이어서_조회된다(int limit) {
        ResponseEntity<ApiResponse<GameListResponseDto>> firstResponse = restTemplate.exchange(
                "/games?limit=" + limit,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        GameListResponseDto firstGameListResponse = (GameListResponseDto) firstResponse.getBody().getData();
        List<GameListResponseDto.GameDto> firstPageGames = firstGameListResponse.games();

        GameListResponseDto.GameDto last = firstPageGames.getLast();

        String url = String.format(
                "/games?cursorPlayCount=%d&cursorUpdatedAt=%s&cursorGameId=%s&limit=%d",
                last.playCount(),
                last.updatedAt(),
                last.gameId(),
                limit
        );
        ResponseEntity<ApiResponse<GameListResponseDto>> secondResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        GameListResponseDto secondGameListResponse = (GameListResponseDto) secondResponse.getBody().getData();
        List<GameListResponseDto.GameDto> secondPageGames = secondGameListResponse.games();

        assertThat(secondPageGames).doesNotContainAnyElementsOf(firstPageGames);
    }

    @Test
    void 게임_정보_상세_조회_시_문제들은_순서대로_정렬되어_반환된다() {
        UUID gameId = games.getFirst().getGameId();

        // when
        ResponseEntity<ApiResponse<GameDetailResponseDto>> response = restTemplate.exchange(
                "/games/" + gameId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        GameDetailResponseDto gameDetailResponse = (GameDetailResponseDto) response.getBody().getData();
        List<GameDetailResponseDto.QuestionDto> questions = gameDetailResponse.questions();

        assertThat(questions).isNotEmpty();

        AtomicBoolean isSorted = new AtomicBoolean(true);

        IntStream.range(0, questions.size() - 1).forEach(i -> {
            GameDetailResponseDto.QuestionDto current = questions.get(i);
            GameDetailResponseDto.QuestionDto next = questions.get(i + 1);

            if (current.questionOrder() >= next.questionOrder()) {
                isSorted.set(false);
            }
        });

        assertThat(isSorted.get()).isTrue();
    }

    @Test
    void 없는_게임_정보를_상세_조회_시_404가_발생한다() throws Exception {
        UUID gameId = UUID.randomUUID();

        // when
        ResponseEntity<?> response = restTemplate.exchange(
                "/games/" + gameId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void 게임신규등록_presignedUrl_발급_테스트() {
        PresignedUrlListRequestDto requestDto = new PresignedUrlListRequestDto(
                List.of(
                        new PresignedUrlListRequestDto.PresignedUrlImageDto("test-1.jpg", 0),
                        new PresignedUrlListRequestDto.PresignedUrlImageDto("test-2.png", 3),
                        new PresignedUrlListRequestDto.PresignedUrlImageDto("test-3.png", 1)
                )
        );

        ResponseEntity<ApiResponse<PresignedUrlListResponseDto>> response = restTemplate.exchange(
                "/games/uploads/urls",
                HttpMethod.POST,
                new HttpEntity<>(requestDto, this.headers),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        PresignedUrlListResponseDto presignedUrlListResponse
                = (PresignedUrlListResponseDto) response.getBody().getData();

        assertThat(presignedUrlListResponse.gameId().toString()).isNotBlank();

        List<PresignedUrlListResponseDto.PresignedUrlDto> presignedUrls
                = presignedUrlListResponse.presignedUrls();

        assertThat(presignedUrls).isNotEmpty();

        IntStream.range(0, requestDto.images().size()).forEach(i -> {
            PresignedUrlListRequestDto.PresignedUrlImageDto requestImage = requestDto.images().get(i);
            PresignedUrlListResponseDto.PresignedUrlDto responseImage = presignedUrls.get(i);

            assertThat(responseImage.imageName()).isEqualTo(requestImage.imageName());
            assertThat(responseImage.questionOrder()).isEqualTo(requestImage.questionOrder());
            assertThat(responseImage.url()).isNotBlank();
            assertThat(responseImage.key()).isNotBlank();
        });
    }

    private GameEntity createGame(String title, UserEntity user, long playCount, boolean isShared, boolean isDeleted) {
        GameEntity game = new GameEntity();

        game.setGameId(UUID.randomUUID());
        game.setGameTitle(title);
        game.setGameThumbnailUrl("http://thumbnail.url/" + title);
        game.setGameCreator(user);
        game.setPlayCount(playCount);
        game.setShared(isShared);
        game.setDeleted(isDeleted);
        game.setQuestionCount(6);
        return game;
    }

    private QuestionEntity createQuestion(GameEntity game, int questionOrder, String questionText, String questionAnswer) {
        QuestionEntity question = new QuestionEntity();
        question.setGame(game);
        question.setQuestionOrder(questionOrder);
        question.setImageUrl("http://image.url/question");
        question.setQuestionText(questionText);
        question.setQuestionAnswer(questionAnswer);
        return question;
    }
}