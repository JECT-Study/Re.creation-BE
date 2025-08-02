package org.ject.recreation.core.api.controller;

import org.ject.recreation.core.api.controller.request.MyGameListRequestDto;
import org.ject.recreation.core.api.controller.response.MyGameListResponseDto;
import org.ject.recreation.core.api.controller.session.SessionUserInfo;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
import org.ject.recreation.core.domain.user.MyGameListResult;
import org.ject.recreation.core.domain.user.UserService;
import org.ject.recreation.core.support.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me/games")
    public ApiResponse<MyGameListResponseDto> getMyGameList(@SessionUserInfo SessionUserInfoDto userInfo,
                                                            @ModelAttribute MyGameListRequestDto request) {
        MyGameListResult myGameListResult = userService.getMyGameList(userInfo.getEmail(), request.toMyGameListQuery());

        return ApiResponse.success(new MyGameListResponseDto(
                myGameListResult.games().stream()
                        .map(game -> new MyGameListResponseDto.MyGameDto(
                                game.gameId(),
                                game.gameThumbnailUrl(),
                                game.gameTitle(),
                                game.questionCount(),
                                game.isShared(),
                                game.playCount(),
                                game.updatedAt()
                        ))
                        .toList()
        ));
    }
}
