package org.ject.recreation.core.api.controller;

import org.ject.recreation.core.api.controller.request.CreateGameRequest;
import org.ject.recreation.core.api.controller.request.GameListRequestDto;
import org.ject.recreation.core.api.controller.response.GameDetailResponseDto;
import org.ject.recreation.core.api.controller.response.GameListResponseDto;
import org.ject.recreation.core.api.controller.session.SessionUserInfo;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
import org.ject.recreation.core.domain.game.GameDetailResult;
import org.ject.recreation.core.domain.game.GameListResult;
import org.ject.recreation.core.domain.game.GameService;
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

    @PostMapping
    public ApiResponse<String> createGame(@SessionUserInfo SessionUserInfoDto userInfo,
                                     @RequestBody CreateGameRequest createGameRequest) {
        return ApiResponse.success(gameService.createGame(userInfo, createGameRequest));
    }

    @PostMapping("/{gameId}/plays")
    public ApiResponse<String> playGame(@PathVariable UUID gameId){
        return ApiResponse.success(gameService.playGame(gameId));
    }

    @PutMapping("/games/{gameId}")
    public ApiResponse<String> updateGame(@SessionUserInfo SessionUserInfoDto userInfo,
                                 @PathVariable UUID gameId,
                                 @RequestBody CreateGameRequest createGameRequest) {
        return ApiResponse.success(gameService.updateGame(userInfo, gameId, createGameRequest));
    }

}
