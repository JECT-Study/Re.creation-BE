package org.ject.recreation.core.api.controller.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.QuestionEntity;
import org.ject.recreation.storage.db.core.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGameRequest {
    private UUID gameId;
    private String gameTitle;
    private String gameThumbnailUrl;
    private List<QuestionRequest> questions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionRequest {
        private String imageUrl;
        private int questionOrder;
        private String questionText;
        private String questionAnswer;
    }

    public GameEntity toGameEntity(UserEntity user) {

        GameEntity game = GameEntity.builder()
                .gameId(gameId)
                .gameCreator(user)
                .gameTitle(gameTitle)
                .gameThumbnailUrl(gameThumbnailUrl)
                .build();

        questions.forEach(questionEntity -> {
            QuestionEntity build = QuestionEntity.builder()
                    .questionOrder(questionEntity.getQuestionOrder())
                    .questionText(questionEntity.getQuestionText())
                    .questionAnswer(questionEntity.getQuestionAnswer())
                    .imageUrl(questionEntity.getImageUrl())
                    .build();
            game.addQuestion(build);
        });
        return game;
    }
}
