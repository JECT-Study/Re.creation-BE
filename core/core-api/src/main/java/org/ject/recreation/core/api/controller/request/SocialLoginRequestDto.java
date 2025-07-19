package org.ject.recreation.core.api.controller.request;

import lombok.Getter;

@Getter
public class SocialLoginRequestDto {
    private String code;
    private String type;
} 