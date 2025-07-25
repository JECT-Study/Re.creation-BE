package org.ject.recreation.core.api.controller;

import org.ject.recreation.core.api.controller.request.GameListRequestDto;
import org.ject.recreation.core.api.controller.request.PresignedUrlListRequestDto;
import org.ject.recreation.core.api.controller.response.GameDetailResponseDto;
import org.ject.recreation.core.api.controller.response.GameListResponseDto;
import org.ject.recreation.core.api.controller.response.PresignedUrlListResponseDto;
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
    public ApiResponse<GameListResponseDto> getGameList(@ModelAttribute GameListRequestDto request) {
        GameListResult gameListResult = gameService.getGameList(request.toGameListQuery());

        return ApiResponse.success(new GameListResponseDto(
                gameListResult.games().stream()
                        .map(game -> new GameListResponseDto.GameDto(
                                game.gameId(),
                                game.gameThumbnail(),
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
    public ApiResponse<PresignedUrlListResponseDto> getPresignedUrls(@RequestBody PresignedUrlListRequestDto request) {
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
    public ApiResponse<PresignedUrlListResponseDto> getPresignedUrls(@PathVariable UUID gameId, @RequestBody PresignedUrlListRequestDto request) {
        PresignedUrlListResult presignedUrlListResult = gameService.getPresignedUrls(gameId, request.toPresignedUrlQuery());

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

}
