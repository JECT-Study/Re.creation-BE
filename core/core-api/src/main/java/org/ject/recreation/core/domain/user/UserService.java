package org.ject.recreation.core.domain.user;

import lombok.RequiredArgsConstructor;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
import org.ject.recreation.core.domain.game.Game;
import org.ject.recreation.core.domain.game.GameReader;
import org.ject.recreation.core.support.error.CoreException;
import org.ject.recreation.core.support.error.ErrorData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.ject.recreation.core.support.error.ErrorType.GAME_FORBIDDEN;

@Service
@RequiredArgsConstructor
public class UserService {

    private final GameReader gameReader;

    @Transactional(readOnly = true)
    public MyGameListResult getMyGameList(String curUserEmail, MyGameListQuery myGameListQuery) {
        if (myGameListQuery.cursorGameId() != null) {
            Game cursorGame = gameReader.getGameByGameId(myGameListQuery.cursorGameId());

            if (!cursorGame.creatorEmail().equals(curUserEmail)) {
                throw new CoreException(GAME_FORBIDDEN, ErrorData.of("gameId", myGameListQuery.cursorGameId()));
            }
        }

        List<Game> myGames = gameReader.getMyGameList(
                myGameListQuery.toMyGameListCursor(),
                myGameListQuery.limit(),
                curUserEmail
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
