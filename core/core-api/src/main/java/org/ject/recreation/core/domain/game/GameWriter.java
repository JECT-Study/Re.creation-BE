package org.ject.recreation.core.domain.game;

import org.ject.recreation.core.support.error.CoreException;
import org.ject.recreation.core.support.error.ErrorData;
import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.GameRepository;
import org.ject.recreation.storage.db.core.QuestionEntity;
import org.ject.recreation.storage.db.core.UserEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

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

    public GameEntity cloneGame(UUID originalGameId, UUID cloneGameId, UserEntity userEntity) {
        GameEntity originalGameEntity = gameRepository.findById(originalGameId)
                .orElseThrow(() -> new CoreException(GAME_NOT_FOUND, ErrorData.of("gameId", originalGameId)));

        String cloneGameTitle = originalGameEntity.getGameTitle() + " 복사본";
        String cloneGameThumbnailUrl = originalGameEntity.getGameThumbnailUrl() == null ? null :
                originalGameEntity.getGameThumbnailUrl().replace(
                originalGameId.toString(),
                cloneGameId.toString()
        );

        GameEntity cloneGameEntity = GameEntity.builder()
                .gameId(cloneGameId)
                .gameCreator(userEntity)
                .gameTitle(cloneGameTitle)
                .gameThumbnailUrl(cloneGameThumbnailUrl)
                .isShared(false)
                .isDeleted(false)
                .questionCount(originalGameEntity.getQuestionCount())
                .playCount(0)
                .build();

        originalGameEntity.getQuestions().forEach(questionEntity -> {
            String cloneImageUrl = questionEntity.getImageUrl() == null ? null :
                    questionEntity.getImageUrl().replace(
                    originalGameId.toString(),
                    cloneGameId.toString()
            );
            QuestionEntity build = QuestionEntity.builder()
                    .questionOrder(questionEntity.getQuestionOrder())
                    .questionText(questionEntity.getQuestionText())
                    .questionAnswer(questionEntity.getQuestionAnswer())
                    .imageUrl(cloneImageUrl)
                    .build();
            cloneGameEntity.addQuestion(build);
        });

        return gameRepository.save(cloneGameEntity);
    }

}
