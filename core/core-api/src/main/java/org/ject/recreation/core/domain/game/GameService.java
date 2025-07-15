package org.ject.recreation.core.domain.game;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final GameReader gameReader;

    public GameService(GameReader gameReader) {
        this.gameReader = gameReader;
    }

    public GameListResult getGameList(GameListQuery gameListQuery) {
        List<GameListItem> gameListItems = gameReader.getGameList(
                gameListQuery.toGameListCursor(),
                gameListQuery.limit(),
                gameListQuery.query());

        return new GameListResult(gameListItems);
    }
}
