package org.ject.recreation.core.api.controller;

import org.ject.recreation.core.api.controller.response.GameListResponseDto;
import org.ject.recreation.core.support.response.ApiResponse;
import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

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

    private final List<GameEntity> games = List.of(
            createGame("가장 인기 퀴즈", 400,true, false),
            createGame("OX 퀴즈", 310, true, false),
            createGame("O/X 퀴즈", 300, true, false),
            createGame("인물 퀴즈 2", 250, true, false),
            createGame("최근 업데이트 퀴즈", 210, true, false),
            createGame("영화 명대사 퀴즈  1", 200, true, false),
            createGame("영화 명대사 퀴즈  2 (비공유)", 200, false, false),
            createGame("연상 퀴즈", 180, true, false),
            createGame("지구과학 퀴즈 1 (삭제)", 150, true, true),
            createGame("지구과학 퀴즈 2 (삭제)", 150, true, true),
            createGame("한국사 퀴즈", 120, true, false),
            createGame("지구과학 퀴즈 3 (비공유)", 110, false, false),
            createGame("인물 퀴즈 1 (삭제)", 100, true, true),
            createGame("인물 퀴즈 2 (비공유 삭제)", 95, false, true),
            createGame("수학 퀴즈", 90, true, false),
            createGame("세계사 퀴즈", 80,  true, false),
            createGame("영어 퀴즈", 70, true, false),
            createGame("과학 퀴즈", 60, true, false),
            createGame("영화 명대사 퀴즈 3", 50, true, false),
            createGame("랜덤 퀴즈", 50, true, false)
    );

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();

        // 테스트용 데이터 삽입
        gameRepository.saveAll(games);
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


    private GameEntity createGame(String title, long playCount, boolean isShared, boolean isDeleted) {
        GameEntity game = new GameEntity();
        game.setGameId(UUID.randomUUID());
        game.setGameTitle(title);
        game.setGameThumbnailUrl("http://thumbnail.url/" + title);
        game.setGameCreatorEmail("test@example.com");
        game.setPlayCount(playCount);
        game.setShared(isShared);
        game.setDeleted(isDeleted);
        game.setQuestionCount(10);
        game.setVersion(1);
        return game;
    }
}

