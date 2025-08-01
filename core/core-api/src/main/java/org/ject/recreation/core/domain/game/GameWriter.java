package org.ject.recreation.core.domain.game;

import org.ject.recreation.core.support.error.CoreException;
import org.ject.recreation.core.support.error.ErrorData;
import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.GameRepository;
import org.springframework.stereotype.Component;

import static org.ject.recreation.core.support.error.ErrorType.GAME_IS_DELETED;
import static org.ject.recreation.core.support.error.ErrorType.GAME_NOT_FOUND;

@Component
public class GameWriter {

    private final GameRepository gameRepository;

    public GameWriter(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public void deleteGame(Game game) {
        GameEntity gameEntity = gameRepository.findById(game.gameId())
                .orElseThrow(() -> new CoreException(GAME_NOT_FOUND, ErrorData.of("gameId", game.gameId())));
        gameEntity.softDelete();
    }

    public void shareGame(Game game) {
        GameEntity gameEntity = gameRepository.findById(game.gameId())
                .orElseThrow(() -> new CoreException(GAME_NOT_FOUND, ErrorData.of("gameId", game.gameId())));

        if (gameEntity.isDeleted()) {
            throw new CoreException(GAME_IS_DELETED, ErrorData.of("gameId", game.gameId()));
        }

        gameEntity.share();
    }

    public void unShareGame(Game game) {
        GameEntity gameEntity = gameRepository.findById(game.gameId())
                .orElseThrow(() -> new CoreException(GAME_NOT_FOUND, ErrorData.of("gameId", game.gameId())));

        if (gameEntity.isDeleted()) {
            throw new CoreException(GAME_IS_DELETED, ErrorData.of("gameId", game.gameId()));
        }

        gameEntity.unShare();
    }

}
