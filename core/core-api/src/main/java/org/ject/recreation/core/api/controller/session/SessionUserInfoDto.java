package org.ject.recreation.core.api.controller.session;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionUserInfoDto {
    private String email;
    private String nickname;
    private String profileImageUrl;
} 