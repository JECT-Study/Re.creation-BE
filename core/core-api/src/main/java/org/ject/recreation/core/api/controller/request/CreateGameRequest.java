package org.ject.recreation.core.api.controller.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.ject.recreation.storage.db.core.GameEntity;

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
    private String gameCreatorEmail;
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
}
