package org.ject.recreation.core.api.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.ject.recreation.core.api.controller.validation.UniqueQuestionOrder;
import org.ject.recreation.core.api.controller.validation.contract.HasOrder;
import org.ject.recreation.core.api.controller.validation.contract.HasOrderedItems;
import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.QuestionEntity;
import org.ject.recreation.storage.db.core.UserEntity;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@UniqueQuestionOrder
public class CreateGameRequest implements HasOrderedItems {

    @NotNull
    private UUID gameId;

    @NotBlank
    private String gameTitle;

    @NotBlank
    private String gameThumbnailUrl;

    @NotNull
    @Size(min = 1)
    @Valid
    private List<QuestionRequest> questions;

    @JsonIgnore
    @Override
    public List<? extends HasOrder> getOrderedItems() {
        return questions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionRequest implements HasOrder {
        private String imageUrl;

        @Min(0)
        private int questionOrder;

        @NotBlank
        private String questionText;

        @NotBlank
        private String questionAnswer;

        @Override
        public int getOrder() {
            return questionOrder;
        }
    }

    public GameEntity toGameEntity(UserEntity user) {

        GameEntity game = GameEntity.builder()
                .gameId(gameId)
                .gameCreator(user)
                .gameTitle(gameTitle)
                .gameThumbnailUrl(gameThumbnailUrl)
                .questionCount(questions.size())
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
