package org.ject.recreation.core.domain.user;

import lombok.RequiredArgsConstructor;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
import org.ject.recreation.core.domain.game.Game;
import org.ject.recreation.core.domain.game.GameReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final GameReader gameReader;

    @Transactional(readOnly = true)
    public MyGameListResult getMyGameList(SessionUserInfoDto userInfo, MyGameListQuery myGameListQuery) {
        List<Game> myGames = gameReader.getMyGameList(
                myGameListQuery.toMyGameListCursor(),
                myGameListQuery.limit(),
                userInfo.getEmail()
        );

        return new MyGameListResult(myGames.stream()
                .map(game -> new MyGameListResult.MyGameResult(
                        game.gameId(),
                        game.gameThumbnailUrl(),
                        game.gameTitle(),
                        game.questionCount(),
                        game.isShared(),
                        game.playCount(),
                        game.updatedAt()))
                .toList());
    }

}
