package org.ject.recreation.core.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SocialLoginRequestDto {

    @NotBlank
    private String code;

    @NotBlank
    private String type;
} 