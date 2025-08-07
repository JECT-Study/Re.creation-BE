package org.ject.recreation.core.api.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.ject.recreation.core.api.controller.validation.UniqueQuestionOrder;
import org.ject.recreation.core.api.controller.validation.contract.HasOrder;
import org.ject.recreation.core.api.controller.validation.contract.HasOrderedItems;
import org.ject.recreation.core.domain.game.upload.PresignedUrlQuery;

import java.util.List;

@UniqueQuestionOrder
public record PresignedUrlListRequestDto(
        @NotNull
        @Valid
        List<PresignedUrlImageDto> images
) implements HasOrderedItems {

    @JsonIgnore
    @Override
    public List<? extends HasOrder> getOrderedItems() {
        return images;
    }

    public record PresignedUrlImageDto(

            @NotBlank
            String imageName,

            @Min(0)
            int questionOrder

    ) implements HasOrder {

        @Override
        public int getOrder() {
            return questionOrder;
        }

    }

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
