package org.ject.recreation.core.api.controller;

import org.ject.recreation.core.api.controller.request.CreateGameRequest;
import org.ject.recreation.core.api.controller.request.PresignedUrlListRequestDto;
import org.ject.recreation.core.api.controller.request.UpdateGameRequest;
import org.ject.recreation.core.api.controller.response.GameDetailResponseDto;
import org.ject.recreation.core.api.controller.response.GameListResponseDto;
import org.ject.recreation.core.api.controller.response.MyGameListResponseDto;
import org.ject.recreation.core.api.controller.response.PresignedUrlListResponseDto;
import org.ject.recreation.core.support.response.ApiResponse;
import org.ject.recreation.storage.db.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
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

    private UserEntity me;

    private UserEntity otherUser;

    private List<GameEntity> games;

    private List<QuestionEntity> questions;

    @Value("${default-image.game-thumbnail-url}")
    private String defaultGameThumbnailUrl;

    @BeforeEach
    void initializeData() {
        me = new UserEntity(
                "test@example.com",
                "kakao",
                "http://image.url/question",
                "테스트유저1",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        otherUser = new UserEntity(
                "test2@google.com",
                "kakao",
                "http://image.url/question",
                "테스트유저2",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        userRepository.save(me);
        userRepository.save(otherUser);

        games = List.of(
                createGame("가장 인기 퀴즈", me, 400,true, false),
                createGame("OX 퀴즈", me, 310, true, false),
                createGame("O/X 퀴즈", me, 300, true, false),
                createGame("인물 퀴즈 2", me, 250, true, false),
                createGame("최근 업데이트 퀴즈", me, 210, true, false),
                createGame("영화 명대사 퀴즈  1", me, 200, true, false),
                createGame("영화 명대사 퀴즈  2 (비공유)", me, 200, false, false),
                createGame("연상 퀴즈", me, 180, true, false),
                createGame("지구과학 퀴즈 1 (삭제)", me, 150, true, true),
                createGame("지구과학 퀴즈 2 (삭제)", me, 150, true, true),
                createGame("한국사 퀴즈", otherUser, 120, true, false),
                createGame("지구과학 퀴즈 3 (비공유)", otherUser, 110, false, false),
                createGame("인물 퀴즈 1 (삭제)", otherUser, 100, true, true),
                createGame("인물 퀴즈 2 (비공유 삭제)", otherUser, 95, false, true),
                createGame("수학 퀴즈", otherUser, 90, true, false),
                createGame("세계사 퀴즈", otherUser, 80,  true, false),
                createGame("영어 퀴즈", otherUser, 70, true, false),
                createGame("과학 퀴즈", otherUser, 60, true, false),
                createGame("영화 명대사 퀴즈 3", otherUser, 50, true, false),
                createGame("랜덤 퀴즈", otherUser, 50, true, false)
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

    @Nested
    @DisplayName("게임 목록 조회 API 테스트")
    class GameListApiTest {
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
        void 커서로_없는_게임을_주면_404가_발생한다() {
            String url = String.format(
                    "/games?cursorPlayCount=%d&cursorUpdatedAt=%s&cursorGameId=%s&limit=%d",
                    0, LocalDateTime.now(), UUID.randomUUID(), 10
            );

            ResponseEntity<?> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void 커서필드를_모두_제공하지_않으면_400이_발생한다() {
            String url = String.format(
                    "/games?cursorPlayCount=%d&cursorUpdatedAt=%s&limit=%d",
                    0, LocalDateTime.now(), 10
            );

            ResponseEntity<?> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("게임 상세 조회 API 테스트")
    class GameDetailApiTest {
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
    }

    @Nested
    @DisplayName("게임 플레이 API 테스트")
    class GamePlayApiTest {
        @Test
        void 게임_플레이_테스트() {
            UUID gameId = games.getFirst().getGameId();
            long initialPlayCount = gameRepository.findById(gameId)
                    .orElseThrow().getPlayCount();
            long initialVersion = gameRepository.findById(gameId)
                    .orElseThrow().getVersion();

            // when
            ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                    "/games/" + gameId + "/plays",
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            long updatedPlayCount = gameRepository.findById(gameId)
                    .orElseThrow().getPlayCount();
            long updatedVersion = gameRepository.findById(gameId)
                    .orElseThrow().getVersion();

            assertThat(updatedPlayCount).isEqualTo(initialPlayCount + 1);
            assertThat(updatedVersion).isEqualTo(initialVersion);
        }

        @Test
        void 없는_게임을_플레이하려고_하면_404가_발생한다() {
            UUID nonExistentGameId = UUID.randomUUID();

            // when
            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + nonExistentGameId + "/plays",
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("S3 Presinged URL 발급 API 테스트")
    class S3PresignedUrlApiTest {

        private PresignedUrlListRequestDto presignedUrlListRequest;

        @BeforeEach
        void setUp() {
            setHeaders();

            presignedUrlListRequest = new PresignedUrlListRequestDto(
                    List.of(
                            new PresignedUrlListRequestDto.PresignedUrlImageDto("test-1.jpg", 0),
                            new PresignedUrlListRequestDto.PresignedUrlImageDto("test-2.png", 3),
                            new PresignedUrlListRequestDto.PresignedUrlImageDto("test-3.png", 1)
                    )
            );
        }

        @Test
        void 게임신규등록_presignedUrl_발급_테스트() {
            ResponseEntity<ApiResponse<PresignedUrlListResponseDto>> response = restTemplate.exchange(
                    "/games/uploads/urls",
                    HttpMethod.POST,
                    new HttpEntity<>(presignedUrlListRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            PresignedUrlListResponseDto presignedUrlListResponse
                    = (PresignedUrlListResponseDto) response.getBody().getData();

            assertThat(presignedUrlListResponse.gameId().toString()).isNotBlank();

            List<PresignedUrlListResponseDto.PresignedUrlDto> presignedUrls
                    = presignedUrlListResponse.presignedUrls();
            UUID gameId = presignedUrlListResponse.gameId();

            assertThat(presignedUrls).isNotEmpty();

            IntStream.range(0, presignedUrlListRequest.images().size()).forEach(i -> {
                PresignedUrlListRequestDto.PresignedUrlImageDto requestImage = presignedUrlListRequest.images().get(i);
                PresignedUrlListResponseDto.PresignedUrlDto responseImage = presignedUrls.get(i);

                assertThat(responseImage.imageName()).isEqualTo(requestImage.imageName());
                assertThat(responseImage.questionOrder()).isEqualTo(requestImage.questionOrder());
                assertThat(responseImage.url()).isNotBlank();
                assertThat(responseImage.key()).isNotBlank();
                assertThat(responseImage.key().startsWith("games/" + gameId)).isTrue();
            });
        }

        @Test
        void 기존게임수정_presignedUrl_발급_테스트() {
            UUID gameId = games.getFirst().getGameId();

            ResponseEntity<ApiResponse<PresignedUrlListResponseDto>> response = restTemplate.exchange(
                    "/games/" + gameId + "/uploads/urls",
                    HttpMethod.POST,
                    new HttpEntity<>(presignedUrlListRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            PresignedUrlListResponseDto presignedUrlListResponse
                    = (PresignedUrlListResponseDto) response.getBody().getData();

            assertThat(presignedUrlListResponse.gameId()).isEqualTo(gameId);

            List<PresignedUrlListResponseDto.PresignedUrlDto> presignedUrls
                    = presignedUrlListResponse.presignedUrls();

            assertThat(presignedUrls).isNotEmpty();

            IntStream.range(0, presignedUrlListRequest.images().size()).forEach(i -> {
                PresignedUrlListRequestDto.PresignedUrlImageDto requestImage = presignedUrlListRequest.images().get(i);
                PresignedUrlListResponseDto.PresignedUrlDto responseImage = presignedUrls.get(i);

                assertThat(responseImage.imageName()).isEqualTo(requestImage.imageName());
                assertThat(responseImage.questionOrder()).isEqualTo(requestImage.questionOrder());
                assertThat(responseImage.url()).isNotBlank();
                assertThat(responseImage.key()).isNotBlank();
                assertThat(responseImage.key().startsWith("games/" + gameId)).isTrue();
            });
        }

        @Test
        void 내가_만들지_않은_게임에_대해_presignedUrl_발급을_요청하면_403이_발생한다() {
            UUID otherUserGameId = games.stream()
                    .filter(game -> !game.getGameCreator().getEmail().equals(me.getEmail()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("다른 사용자의 게임이 없습니다."))
                    .getGameId();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + otherUserGameId + "/uploads/urls",
                    HttpMethod.POST,
                    new HttpEntity<>(presignedUrlListRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void 없는_게임에_대해_presignedUrl_발급을_요청하면_404가_발생한다() {
            UUID nonExistentGameId = UUID.randomUUID();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + nonExistentGameId + "/uploads/urls",
                    HttpMethod.POST,
                    new HttpEntity<>(presignedUrlListRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void 게임신규등록_presignedUrl_발급시_이미지_순서가_중복되면_400이_발생한다() {
            PresignedUrlListRequestDto invalidRequest = new PresignedUrlListRequestDto(
                    List.of(
                            new PresignedUrlListRequestDto.PresignedUrlImageDto("test-1.jpg", 0),
                            new PresignedUrlListRequestDto.PresignedUrlImageDto("test-2.png", 0) // 중복된 순서
                    )
            );

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/uploads/urls",
                    HttpMethod.POST,
                    new HttpEntity<>(invalidRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void 기존게임수정_presignedUrl_발급시_이미지_순서가_중복되면_400이_발생한다() {
            UUID gameId = games.getFirst().getGameId();

            PresignedUrlListRequestDto invalidRequest = new PresignedUrlListRequestDto(
                    List.of(
                            new PresignedUrlListRequestDto.PresignedUrlImageDto("test-1.jpg", 0),
                            new PresignedUrlListRequestDto.PresignedUrlImageDto("test-2.png", 0) // 중복된 순서
                    )
            );

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + gameId + "/uploads/urls",
                    HttpMethod.POST,
                    new HttpEntity<>(invalidRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("게임 삭제 API 테스트")
    class GameDeleteApiTest {
        @BeforeEach
        void setUp() {
            setHeaders();
        }

        @Test
        void 게임_삭제시_실제로_삭제되진_않는다() {
            UUID gameId = games.getFirst().getGameId();

            ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                    "/games/" + gameId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            GameEntity deletedGame = gameRepository.findById(gameId).orElse(null);
            assertThat(deletedGame).isNotNull();
            assertThat(deletedGame.isDeleted()).isTrue();
            assertThat(deletedGame.getDeletedAt()).isNotNull();
        }

        @Test
        void 내가_만들지_않은_게임을_삭제하려고_하면_403이_발생한다() {
            UUID otherUserGameId = games.stream()
                    .filter(game -> !game.getGameCreator().getEmail().equals(me.getEmail()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("다른 사용자의 게임이 없습니다."))
                    .getGameId();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + otherUserGameId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void 없는_게임을_삭제하려고_하면_404가_발생한다() {
            UUID nonExistentGameId = UUID.randomUUID();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + nonExistentGameId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("게임 공유/비공유 API 테스트")
    class GameShareApiTest {
        @BeforeEach
        void setUp() {
            setHeaders();
        }

        @Test
        void 게임_공유_시_공유상태로_변경된다() {
            UUID unsharedGameId = games.stream()
                    .filter(game -> !game.isShared())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("비공유 게임이 없습니다."))
                    .getGameId();

            long initialVersion = gameRepository.findById(unsharedGameId)
                    .orElseThrow().getVersion();

            ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                    "/games/" + unsharedGameId + "/share",
                    HttpMethod.POST,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            GameEntity sharedGame = gameRepository.findById(unsharedGameId).orElse(null);
            long updatedVersion = sharedGame.getVersion();

            assertThat(sharedGame).isNotNull();
            assertThat(sharedGame.isShared()).isTrue();
            assertThat(updatedVersion).isEqualTo(initialVersion);
        }

        @Test
        void 게임_비공유_시_비공유상태로_변경된다() {
            UUID sharedGameId = games.stream()
                    .filter(GameEntity::isShared)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("공유된 게임이 없습니다."))
                    .getGameId();

            long initialVersion = gameRepository.findById(sharedGameId)
                    .orElseThrow().getVersion();

            ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                    "/games/" + sharedGameId + "/unshare",
                    HttpMethod.POST,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            GameEntity unsharedGame = gameRepository.findById(sharedGameId).orElse(null);
            long updatedVersion = unsharedGame.getVersion();

            assertThat(unsharedGame).isNotNull();
            assertThat(unsharedGame.isShared()).isFalse();
            assertThat(updatedVersion).isEqualTo(initialVersion);
        }

        @Test
        void 내가_만들지_않은_게임을_공유하려고_하면_403이_발생한다() {
            UUID otherUserGameId = games.stream()
                    .filter(game -> !game.getGameCreator().getEmail().equals(me.getEmail()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("다른 사용자의 게임이 없습니다."))
                    .getGameId();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + otherUserGameId + "/share",
                    HttpMethod.POST,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void 내가_만들지_않은_게임을_비공유하려고_하면_403이_발생한다() {
            UUID otherUserGameId = games.stream()
                    .filter(game -> !game.getGameCreator().getEmail().equals(me.getEmail()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("다른 사용자의 게임이 없습니다."))
                    .getGameId();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + otherUserGameId + "/unshare",
                    HttpMethod.POST,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void 없는_게임을_공유하려고_하면_404가_발생한다() {
            UUID nonExistentGameId = UUID.randomUUID();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + nonExistentGameId + "/share",
                    HttpMethod.POST,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void 없는_게임을_비공유하려고_하면_404가_발생한다() {
            UUID nonExistentGameId = UUID.randomUUID();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + nonExistentGameId + "/unshare",
                    HttpMethod.POST,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void 삭제된_게임을_공유하려고_하면_404가_발생한다() {
            UUID deletedGameId = games.stream()
                    .filter(GameEntity::isDeleted)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("삭제된 게임이 없습니다."))
                    .getGameId();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + deletedGameId + "/share",
                    HttpMethod.POST,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void 삭제된_게임을_비공유하려고_하면_404가_발생한다() {
            UUID deletedGameId = games.stream()
                    .filter(GameEntity::isDeleted)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("삭제된 게임이 없습니다."))
                    .getGameId();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + deletedGameId + "/unshare",
                    HttpMethod.POST,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("내 게임 목록 조회 API 테스트")
    class MyGameListApiTest {
        @BeforeEach
        void setUp() {
            setHeaders();
        }

        @Test
        void 내_게임_목록_조회시_내_게임만_반환된다() {
            System.out.println("내 게임 목록 조회 테스트 시작");

            ResponseEntity<ApiResponse<MyGameListResponseDto>> response = restTemplate.exchange(
                    "/users/me/games?limit=10",
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            MyGameListResponseDto myGameListResponse = (MyGameListResponseDto) response.getBody().getData();
            List<MyGameListResponseDto.MyGameDto> games = myGameListResponse.games();

            assertThat(games).isNotEmpty();

            assertThat(games.stream().allMatch(game -> {
                GameEntity gameEntity = gameRepository.findById(game.gameId())
                        .orElseThrow(() -> new RuntimeException("게임을 찾을 수 없습니다."));
                return gameEntity.getGameCreator().getEmail().equals(me.getEmail());
            })).isTrue();
        }

        @Test
        void 내_게임_목록_조회시_삭제되지_않은_게임만_반환된다() {
            ResponseEntity<ApiResponse<MyGameListResponseDto>> response = restTemplate.exchange(
                    "/users/me/games?limit=10",
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            MyGameListResponseDto myGameListResponse = (MyGameListResponseDto) response.getBody().getData();
            List<MyGameListResponseDto.MyGameDto> games = myGameListResponse.games();

            assertThat(games).isNotEmpty();

            assertThat(games.stream().allMatch(game -> {
                GameEntity gameEntity = gameRepository.findById(game.gameId())
                        .orElseThrow(() -> new RuntimeException("게임을 찾을 수 없습니다."));
                return gameEntity.isDeleted() == false;
            })).isTrue();
        }

        @Test
        void 내_게임_목록_조회시_최근수정일시_기준_내림차순으로_반환된다() {
            ResponseEntity<ApiResponse<MyGameListResponseDto>> response = restTemplate.exchange(
                    "/users/me/games?limit=100",
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            MyGameListResponseDto myGameListResponse = (MyGameListResponseDto) response.getBody().getData();
            List<MyGameListResponseDto.MyGameDto> games = myGameListResponse.games();

            assertThat(games).isNotEmpty();

            AtomicBoolean isSorted = new AtomicBoolean(true);

            IntStream.range(0, games.size() - 1).forEach(i -> {
                MyGameListResponseDto.MyGameDto current = games.get(i);
                MyGameListResponseDto.MyGameDto next = games.get(i + 1);

                if (!current.updatedAt().isAfter(next.updatedAt())) {
                    isSorted.set(false);
                }
            });

            assertThat(isSorted.get()).isTrue();
        }

        @Test
        void 커서기반_페이징_정상작동하고_중복없이_이어서_조회된다() {
            ResponseEntity<ApiResponse<MyGameListResponseDto>> firstResponse = restTemplate.exchange(
                    "/users/me/games?limit=4",
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            MyGameListResponseDto firstMyGameListResponse = (MyGameListResponseDto) firstResponse.getBody().getData();
            List<MyGameListResponseDto.MyGameDto> firstPageMyGames = firstMyGameListResponse.games();
            MyGameListResponseDto.MyGameDto last = firstPageMyGames.getLast();

            String url = String.format(
                    "/users/me/games?cursorUpdatedAt=%s&cursorGameId=%s&limit=4",
                    last.updatedAt(),
                    last.gameId()
            );

            ResponseEntity<ApiResponse<MyGameListResponseDto>> secondResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            MyGameListResponseDto secondMyGameListResponse = (MyGameListResponseDto) secondResponse.getBody().getData();
            List<MyGameListResponseDto.MyGameDto> secondPageMyGames = secondMyGameListResponse.games();

            assertThat(secondPageMyGames).doesNotContainAnyElementsOf(firstPageMyGames);
        }

        @Test
        void cursor로_준_게임이_내가_만들지_않은_게임이라면_403을_반환한다() {
            LocalDateTime cursorUpdatedAt = LocalDateTime.now().plusDays(1); // 미래의 시간으로 설정
            UUID otherUserGameId = games.stream()
                    .filter(game -> !game.getGameCreator().getEmail().equals(me.getEmail()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("다른 사용자의 게임이 없습니다."))
                    .getGameId();

            ResponseEntity<?> response = restTemplate.exchange(
                    String.format("/users/me/games?cursorUpdatedAt=%s&cursorGameId=%s&limit=10",
                            cursorUpdatedAt, otherUserGameId),
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void cursor로_준_게임이_없으면_404를_반환한다() {
            LocalDateTime cursorUpdatedAt = LocalDateTime.now().plusDays(1); // 미래의 시간으로 설정
            UUID cursorGameId = UUID.randomUUID(); // 존재하지 않는 게임 ID

            ResponseEntity<?> response = restTemplate.exchange(
                    String.format("/users/me/games?cursorUpdatedAt=%s&cursorGameId=%s&limit=10",
                            cursorUpdatedAt, cursorGameId),
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void 커서필드를_모두_제공하지_않으면_400이_발생한다() {
            LocalDateTime cursorUpdatedAt = LocalDateTime.now().plusDays(1); // 미래의 시간으로 설정

            ResponseEntity<?> response = restTemplate.exchange(
                    String.format("/users/me/games?cursorUpdatedAt=%s&limit=10",
                            cursorUpdatedAt),
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("게임 생성 API 테스트")
    class GameCreateApiTest {
        private CreateGameRequest createGameRequest;

        @BeforeEach
        void setUp() {
            setHeaders();
            createGameRequest = CreateGameRequest.builder()
                    .gameId(UUID.randomUUID())
                    .gameTitle("NEW GAME")
                    .gameThumbnailUrl("http://thumbnail.url/test-game")
                    .questions(List.of(
                            new CreateGameRequest.QuestionRequest("http://image.url/question1", 0, "질문 1", "답변 1"),
                            new CreateGameRequest.QuestionRequest("http://image.url/question2", 1, "질문 2", "답변 2"),
                            new CreateGameRequest.QuestionRequest("http://image.url/question3", 2, "질문 3", "답변 3")
                    ))
                    .build();
        }

        @Test
        void 게임_생성_테스트() {
            ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                    "/games",
                    HttpMethod.POST,
                    new HttpEntity<>(createGameRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            GameEntity createdGame = gameRepository.findById(createGameRequest.getGameId())
                    .orElseThrow(() -> new RuntimeException("게임을 찾을 수 없습니다."));

            assertThat(createdGame).isNotNull();
            assertThat(createdGame.getGameTitle()).isEqualTo(createGameRequest.getGameTitle());
            assertThat(createdGame.getGameThumbnailUrl()).isEqualTo(createGameRequest.getGameThumbnailUrl());
            assertThat(createdGame.getGameCreator().getEmail()).isEqualTo(me.getEmail());
            assertThat(createdGame.isShared()).isFalse(); // 기본값은 false
            assertThat(createdGame.isDeleted()).isFalse(); // 기본값은 false
            assertThat(createdGame.getPlayCount()).isEqualTo(0); // 기본값은 0
            assertThat(createdGame.getQuestionCount()).isEqualTo(createGameRequest.getQuestions().size());

            List<QuestionEntity> createdQuestions = questionRepository.findByGameOrderByQuestionOrder(createdGame);

            IntStream.range(0, createGameRequest.getQuestions().size()).forEach(i -> {
                CreateGameRequest.QuestionRequest questionRequest = createGameRequest.getQuestions().get(i);
                QuestionEntity createdQuestion = createdQuestions.get(i);

                assertThat(createdQuestion.getImageUrl()).isEqualTo(questionRequest.getImageUrl());
                assertThat(createdQuestion.getQuestionOrder()).isEqualTo(questionRequest.getQuestionOrder());
                assertThat(createdQuestion.getQuestionText()).isEqualTo(questionRequest.getQuestionText());
                assertThat(createdQuestion.getQuestionAnswer()).isEqualTo(questionRequest.getQuestionAnswer());
            });
        }

        @Test
        void 썸네일_이미지를_지정하지_않으면_디폴트_이미지가_설정된다() {
            CreateGameRequest requestWithoutThumbnail = CreateGameRequest.builder()
                    .gameId(UUID.randomUUID())
                    .gameTitle("GAME WITHOUT THUMBNAIL")
                    .questions(List.of(
                            new CreateGameRequest.QuestionRequest("http://image.url/question1", 0, "질문 1", "답변 1"),
                            new CreateGameRequest.QuestionRequest("http://image.url/question2", 1, "질문 2", "답변 2")
                    ))
                    .build();

            ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                    "/games",
                    HttpMethod.POST,
                    new HttpEntity<>(requestWithoutThumbnail, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            GameEntity createdGame = gameRepository.findById(requestWithoutThumbnail.getGameId())
                    .orElseThrow(() -> new RuntimeException("게임을 찾을 수 없습니다."));

            assertThat(createdGame.getGameThumbnailUrl()).isEqualTo(defaultGameThumbnailUrl);
        }

        @Test
        void 이미_존재하는_gameId로_게임을_생성하려고_하면_409를_반환한다() {
            createGameRequest.setGameId(games.getFirst().getGameId());

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games",
                    HttpMethod.POST,
                    new HttpEntity<>(createGameRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        void 게임_생성시_문제의_순서가_중복되면_400을_반환한다() {
            CreateGameRequest invalidRequest = CreateGameRequest.builder()
                    .gameId(UUID.randomUUID())
                    .gameTitle("INVALID GAME")
                    .gameThumbnailUrl("http://thumbnail.url/invalid-game")
                    .questions(List.of(
                            new CreateGameRequest.QuestionRequest("http://image.url/question1", 0, "질문 1", "답변 1"),
                            new CreateGameRequest.QuestionRequest("http://image.url/question2", 0, "질문 2", "답변 2") // 중복된 순서
                    ))
                    .build();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games",
                    HttpMethod.POST,
                    new HttpEntity<>(invalidRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("게임 수정 API 테스트")
    class GameUpdateApiTest {

        private UUID gameId;
        private UpdateGameRequest updateGameRequest;

        @BeforeEach
        void setUp() {
            setHeaders();

            gameId = games.getFirst().getGameId(); // 수정할 게임 ID 설정
            updateGameRequest = UpdateGameRequest.builder()
                    .gameTitle("UPDATE GAME")
                    .gameThumbnailUrl("http://thumbnail.url/updated-game")
                    .version(1)
                    .questions(List.of(
                            new UpdateGameRequest.UpdateQuestionRequest("http://image.url/question1", 0, "수정된 질문 1", "수정된 답변 1"),
                            new UpdateGameRequest.UpdateQuestionRequest("http://image.url/question2", 1, "수정된 질문 2", "수정된 답변 2"),
                            new UpdateGameRequest.UpdateQuestionRequest("http://image.url/question3", 2, "수정된 질문 3", "수정된 답변 3"),
                            new UpdateGameRequest.UpdateQuestionRequest("http://image.url/question4", 3, "수정된 질문 4", "수정된 답변 4")
                    ))
                    .build();
        }

        @Test
        void 게임_수정_테스트() {
            ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                    "/games/" + gameId,
                    HttpMethod.PUT,
                    new HttpEntity<>(updateGameRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            GameEntity updatedGame = gameRepository.findById(gameId)
                    .orElseThrow(() -> new RuntimeException("게임을 찾을 수 없습니다."));

            assertThat(updatedGame).isNotNull();
            assertThat(updatedGame.getGameTitle()).isEqualTo(updateGameRequest.getGameTitle());
            assertThat(updatedGame.getGameThumbnailUrl()).isEqualTo(updateGameRequest.getGameThumbnailUrl());
            assertThat(updatedGame.getQuestionCount()).isEqualTo(updateGameRequest.getQuestions().size());

            List<QuestionEntity> updatedQuestions = questionRepository.findByGameOrderByQuestionOrder(updatedGame);

            IntStream.range(0, updateGameRequest.getQuestions().size()).forEach(i -> {
                UpdateGameRequest.UpdateQuestionRequest questionRequest = updateGameRequest.getQuestions().get(i);
                QuestionEntity updatedQuestion = updatedQuestions.get(i);

                assertThat(updatedQuestion.getImageUrl()).isEqualTo(questionRequest.getImageUrl());
                assertThat(updatedQuestion.getQuestionOrder()).isEqualTo(questionRequest.getQuestionOrder());
                assertThat(updatedQuestion.getQuestionText()).isEqualTo(questionRequest.getQuestionText());
                assertThat(updatedQuestion.getQuestionAnswer()).isEqualTo(questionRequest.getQuestionAnswer());
            });
        }

        @Test
        void 썸네일_이미지를_지정하지_않으면_디폴트_이미지가_설정된다() {
            UpdateGameRequest requestWithoutThumbnail = UpdateGameRequest.builder()
                    .gameTitle("UPDATE GAME WITHOUT THUMBNAIL")
                    .version(1)
                    .questions(List.of(
                            new UpdateGameRequest.UpdateQuestionRequest("http://image.url/question1", 0, "수정된 질문 1", "수정된 답변 1"),
                            new UpdateGameRequest.UpdateQuestionRequest("http://image.url/question2", 1, "수정된 질문 2", "수정된 답변 2")
                    ))
                    .build();

            ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                    "/games/" + gameId,
                    HttpMethod.PUT,
                    new HttpEntity<>(requestWithoutThumbnail, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            GameEntity updatedGame = gameRepository.findById(gameId)
                    .orElseThrow(() -> new RuntimeException("게임을 찾을 수 없습니다."));

            assertThat(updatedGame.getGameThumbnailUrl()).isEqualTo(defaultGameThumbnailUrl);
        }

        @Test
        void 내가_만들지_않은_게임을_수정하려고_하면_403을_반환한다() {
            UUID otherUserGameId = games.stream()
                    .filter(game -> !game.getGameCreator().getEmail().equals(me.getEmail()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("다른 사용자의 게임이 없습니다."))
                    .getGameId();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + otherUserGameId,
                    HttpMethod.PUT,
                    new HttpEntity<>(updateGameRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void 없는_게임을_수정하려고_하면_404를_반환한다() {
            UUID nonExistentGameId = UUID.randomUUID();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + nonExistentGameId,
                    HttpMethod.PUT,
                    new HttpEntity<>(updateGameRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void 게임_수정시_예전_버전을_사용하면_409를_반환한다() {
            ResponseEntity<ApiResponse<String>> firstResponse = restTemplate.exchange(
                    "/games/" + gameId,
                    HttpMethod.PUT,
                    new HttpEntity<>(updateGameRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            ResponseEntity<?> secondResponse = restTemplate.exchange(
                    "/games/" + gameId,
                    HttpMethod.PUT,
                    new HttpEntity<>(updateGameRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        void 게임_수정을_동시에_시도하면_하나만_성공한다() throws InterruptedException {
            int THREAD_COUNT = 10;
            List<HttpStatusCode> statuses = new CopyOnWriteArrayList<>();
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

            for (int i = 0; i < THREAD_COUNT; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        ResponseEntity<?> response = restTemplate.exchange(
                                "/games/" + gameId,
                                HttpMethod.PUT,
                                new HttpEntity<>(updateGameRequest, headers),
                                new ParameterizedTypeReference<>() {}
                        );
                        statuses.add(response.getStatusCode());
                    } catch (Exception e) {
                        statuses.add(HttpStatusCode.valueOf(500));
                    } finally {
                        doneLatch.countDown();
                    }

                }).start();
            }

            startLatch.countDown();
            doneLatch.await();

            assertThat(statuses.stream().filter(status -> status == HttpStatus.OK).count()).isEqualTo(1);
            assertThat(statuses.stream().filter(status -> status == HttpStatus.CONFLICT).count()).isEqualTo(THREAD_COUNT - 1);
        }

        @Test
        void 게임_수정시_문제의_순서가_중복되면_400을_반환한다() {
            UpdateGameRequest invalidRequest = UpdateGameRequest.builder()
                    .gameTitle("INVALID UPDATE GAME")
                    .gameThumbnailUrl("http://thumbnail.url/invalid-updated-game")
                    .version(1)
                    .questions(List.of(
                            new UpdateGameRequest.UpdateQuestionRequest("http://image.url/question1", 0, "수정된 질문 1", "수정된 답변 1"),
                            new UpdateGameRequest.UpdateQuestionRequest("http://image.url/question2", 0, "수정된 질문 2", "수정된 답변 2") // 중복된 순서
                    ))
                    .build();

            ResponseEntity<?> response = restTemplate.exchange(
                    "/games/" + gameId,
                    HttpMethod.PUT,
                    new HttpEntity<>(invalidRequest, headers),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

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

    public void setHeaders() {
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
    }
}