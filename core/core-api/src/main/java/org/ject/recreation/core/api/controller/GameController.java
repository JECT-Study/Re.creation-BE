package org.ject.recreation.core.api.controller;

import org.ject.recreation.core.api.controller.request.GameListRequestDto;
import org.ject.recreation.core.api.controller.response.GameListItemResponse;
import org.ject.recreation.core.api.controller.response.GameListResponseDto;
import org.ject.recreation.core.domain.game.GameListResult;
import org.ject.recreation.core.domain.game.GameService;
import org.ject.recreation.core.support.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                        .map(game -> new GameListItemResponse(
                                game.gameId(),
                                game.gameThumbnail(),
                                game.gameTitle(),
                                game.questionCount(),
                                game.playCount(),
                                game.updatedAt().toString()
                        ))
                        .toList()
        ));
    }

}
