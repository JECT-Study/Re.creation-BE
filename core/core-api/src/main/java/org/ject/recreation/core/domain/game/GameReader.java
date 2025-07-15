package org.ject.recreation.core.domain.game;

import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.GameRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GameReader {
    private final GameRepository gameRepository;

    public GameReader(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<GameListItem> getGameList(GameListCursor cursor, int limit, String query) {
        List<GameEntity> games = gameRepository.findGamesWithCursorAndQuery(
                cursor.cursorGameId(),
                cursor.cursorPlayCount(),
                cursor.cursorUpdatedAt(),
                PageRequest.of(0, limit),
                query
        );
        return games.stream()
                .map(gameEntity -> new GameListItem(
                        gameEntity.getGameId(),
                        gameEntity.getGameThumbnailUrl(),
                        gameEntity.getGameTitle(),
                        gameEntity.getQuestionCount(),
                        gameEntity.getPlayCount(),
                        gameEntity.getUpdatedAt()))
                .toList();
    }
}
