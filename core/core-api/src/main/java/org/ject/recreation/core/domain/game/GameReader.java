package org.ject.recreation.core.domain.game;

import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.GameRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class GameReader {
    private final GameRepository gameRepository;

    public GameReader(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getGameList(GameListCursor cursor, int limit, String query) {
        List<GameEntity> games = gameRepository.findGamesWithCursorAndQuery(
                cursor.cursorGameId(),
                cursor.cursorPlayCount(),
                cursor.cursorUpdatedAt(),
                PageRequest.of(0, limit),
                query
        );
        return games.stream()
                .map(Game::from)
                .toList();
    }

    public Game getGameByGameId(UUID gameId) {
        GameEntity gameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

        return Game.from(gameEntity);
    }
}
