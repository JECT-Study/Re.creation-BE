package org.ject.recreation.core.api.controller.session;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.ject.recreation.storage.db.core.UserRole;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionUserInfoDto {
    private String email;
    private String nickname;
    private String profileImageUrl;
    private UserRole role;
} 