package org.ject.recreation.core.api.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.ject.recreation.core.api.controller.validation.UniqueQuestionOrder;
import org.ject.recreation.core.api.controller.validation.contract.HasOrder;
import org.ject.recreation.core.api.controller.validation.contract.HasOrderedItems;
import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.QuestionEntity;
import org.ject.recreation.storage.db.core.UserEntity;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@UniqueQuestionOrder
public class UpdateGameRequest implements HasOrderedItems {

    @NotBlank
    private String gameTitle;

    private String gameThumbnailUrl;

    @Min(1)
    private int version;

    @NotNull
    @Size(min = 1)
    @Valid
    private List<UpdateQuestionRequest> questions;

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
    public static class UpdateQuestionRequest implements HasOrder {
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

    public GameEntity fromGameEntity(UserEntity user, String resolvedGameThumbnailUrl, GameEntity existingGame) {
        existingGame.setGameTitle(gameTitle);
        existingGame.setGameCreator(user);
        existingGame.setGameThumbnailUrl(resolvedGameThumbnailUrl);
        existingGame.setVersion(version);
        existingGame.setQuestionCount(questions.size());

        // 기존 질문 리스트를 클리어하고 새로운 질문들로 교체
        existingGame.getQuestions().clear();
        
        questions.forEach(questionRequest -> {
            QuestionEntity newQuestion = QuestionEntity.builder()
                    .questionOrder(questionRequest.getQuestionOrder())
                    .questionText(questionRequest.getQuestionText())
                    .questionAnswer(questionRequest.getQuestionAnswer())
                    .imageUrl(questionRequest.getImageUrl())
                    .game(existingGame)
                    .build();
            existingGame.getQuestions().add(newQuestion);
        });
        
        return existingGame;
    }
}
