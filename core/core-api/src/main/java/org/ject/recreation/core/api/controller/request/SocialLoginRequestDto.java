package org.ject.recreation.core.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialLoginRequestDto {

    @NotBlank
    private String code;

    @NotBlank
    private String type;
} 