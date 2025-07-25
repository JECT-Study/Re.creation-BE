package org.ject.recreation.core.api.controller.request;

import org.ject.recreation.core.domain.game.upload.PresignedUrlQuery;

import java.util.List;

public record PresignedUrlListRequestDto(
        List<PresignedUrlImageDto> images
) {
    public record PresignedUrlImageDto(
            String imageName,
            int questionOrder
    ) { }

    public PresignedUrlQuery toPresignedUrlQuery() {
        return new PresignedUrlQuery(
                images.stream()
                        .map(image -> new PresignedUrlQuery.QuestionImageQuery(
                                image.imageName,
                                image.questionOrder
                        ))
                        .toList()
        );
    }
}
