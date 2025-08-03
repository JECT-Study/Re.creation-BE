package org.ject.recreation.core.api.controller.request;

import lombok.*;
import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.QuestionEntity;
import org.ject.recreation.storage.db.core.UserEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGameRequest {
    private String gameTitle;
    private String gameThumbnailUrl;
    private int version;
    private List<UpdateQuestionRequest> questions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateQuestionRequest {
        private String imageUrl;
        private int questionOrder;
        private String questionText;
        private String questionAnswer;
    }

    public GameEntity fromGameEntity(UserEntity user, GameEntity existingGame) {
        existingGame.setGameTitle(gameTitle);
        existingGame.setGameCreator(user);
        existingGame.setGameThumbnailUrl(gameThumbnailUrl);
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
