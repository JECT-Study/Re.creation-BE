package org.ject.recreation.core.api.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockUserRequestDto {
    @Builder.Default
    private List<String> email = new ArrayList<>();
}
