package org.ject.recreation.core.api.controller;

import jakarta.validation.Valid;
import org.ject.recreation.core.api.controller.request.CreateGameRequest;
import org.ject.recreation.core.api.controller.request.GameListRequestDto;
import org.ject.recreation.core.api.controller.request.PresignedUrlListRequestDto;
import org.ject.recreation.core.api.controller.request.UpdateGameRequest;
import org.ject.recreation.core.api.controller.response.GameListResponseDto;
import org.ject.recreation.core.api.controller.response.PresignedUrlListResponseDto;
import org.ject.recreation.core.api.controller.response.GameDetailResponseDto;
import org.ject.recreation.core.api.controller.session.SessionUserInfo;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
import org.ject.recreation.core.domain.game.GameDetailResult;
import org.ject.recreation.core.domain.game.GameListResult;
import org.ject.recreation.core.domain.game.GameService;
import org.ject.recreation.core.domain.game.upload.PresignedUrlListResult;
import org.ject.recreation.core.support.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ApiResponse<GameListResponseDto> getGameList(@Valid @ModelAttribute GameListRequestDto request) {
        GameListResult gameListResult = gameService.getGameList(request.toGameListQuery());

        return ApiResponse.success(new GameListResponseDto(
                gameListResult.games().stream()
                        .map(game -> new GameListResponseDto.GameDto(
                                game.gameId(),
                                game.gameThumbnailUrl(),
                                game.gameTitle(),
                                game.questionCount(),
                                game.playCount(),
                                game.updatedAt()
                        ))
                        .toList()
        ));
    }

    @GetMapping("/{gameId}")
    public ApiResponse<GameDetailResponseDto> getGameDetail(@PathVariable UUID gameId) {
        GameDetailResult gameDetailResult = gameService.getGameDetail(gameId);

        return ApiResponse.success(new GameDetailResponseDto(
                gameDetailResult.gameTitle(),
                gameDetailResult.nickname(),
                gameDetailResult.questionCount(),
                gameDetailResult.version(),
                gameDetailResult.questions().stream()
                        .map(question -> new GameDetailResponseDto.QuestionDto(
                                question.questionId(),
                                question.questionOrder(),
                                question.imageUrl(),
                                question.questionText(),
                                question.questionAnswer(),
                                question.version()))
                        .toList()
        ));
    }

    @PostMapping("/uploads/urls")
    public ApiResponse<PresignedUrlListResponseDto> getPresignedUrls(@Valid @RequestBody PresignedUrlListRequestDto request) {
        PresignedUrlListResult presignedUrlListResult = gameService.getPresignedUrls(request.toPresignedUrlQuery());

        return ApiResponse.success(new PresignedUrlListResponseDto(
                presignedUrlListResult.gameId(),
                presignedUrlListResult.presignedUrls().stream()
                        .map(url -> new PresignedUrlListResponseDto.PresignedUrlDto(
                                url.imageName(),
                                url.questionOrder(),
                                url.url(),
                                url.key()))
                        .toList()
        ));
    }

    @PostMapping("/{gameId}/uploads/urls")
    public ApiResponse<PresignedUrlListResponseDto> getPresignedUrls(@SessionUserInfo SessionUserInfoDto userInfo,
                                                                     @PathVariable UUID gameId,
                                                                     @Valid @RequestBody PresignedUrlListRequestDto request) {
        PresignedUrlListResult presignedUrlListResult = gameService.getPresignedUrls(userInfo.getEmail(), gameId, request.toPresignedUrlQuery());

        return ApiResponse.success(new PresignedUrlListResponseDto(
                presignedUrlListResult.gameId(),
                presignedUrlListResult.presignedUrls().stream()
                        .map(url -> new PresignedUrlListResponseDto.PresignedUrlDto(
                                url.imageName(),
                                url.questionOrder(),
                                url.url(),
                                url.key()))
                        .toList()
        ));
    }

    @PostMapping
    public ApiResponse<String> createGame(@SessionUserInfo SessionUserInfoDto userInfo,
                                          @Valid @RequestBody CreateGameRequest createGameRequest) {
        return ApiResponse.success(gameService.createGame(userInfo, createGameRequest));
    }

    @PostMapping("/{gameId}/plays")
    public ApiResponse<String> playGame(@PathVariable UUID gameId){
        return ApiResponse.success(gameService.playGame(gameId));
    }

    @PutMapping("/{gameId}")
    public ApiResponse<String> updateGame(@SessionUserInfo SessionUserInfoDto userInfo,
                                          @PathVariable UUID gameId,
                                          @Valid @RequestBody UpdateGameRequest updateGameRequest) {
        return ApiResponse.success(gameService.updateGame(userInfo, gameId, updateGameRequest));
    }

    @DeleteMapping("/{gameId}")
    public ApiResponse<Void> deleteGame(@SessionUserInfo SessionUserInfoDto userInfo,
                                        @PathVariable UUID gameId) {
        gameService.deleteGame(userInfo.getEmail(), gameId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{gameId}/share")
    public ApiResponse<Void> shareGame(@SessionUserInfo SessionUserInfoDto userInfo,
                                       @PathVariable UUID gameId) {
        gameService.shareGame(userInfo.getEmail(), gameId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{gameId}/unshare")
    public ApiResponse<Void> unShareGame(@SessionUserInfo SessionUserInfoDto userInfo,
                                         @PathVariable UUID gameId) {
        gameService.unShareGame(userInfo.getEmail(), gameId);
        return ApiResponse.success(null);
    }

    @GetMapping("/default")
    public ApiResponse<GameListResponseDto> getDefaultGameList() {
        return ApiResponse.success(gameService.getDefaultGame());
    }
}
