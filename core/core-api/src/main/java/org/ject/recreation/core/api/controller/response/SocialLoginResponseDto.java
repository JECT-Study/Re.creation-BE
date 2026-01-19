package org.ject.recreation.core.api.controller.response;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.ject.recreation.storage.db.core.UserRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialLoginResponseDto {
    private String profileImageUrl;
    private String nickname;
    private String email;
    private UserRole role;

    @Override
    public String toString() {
        return "SocialLoginResponseDto{" +
                "profileImageUrl='" + profileImageUrl + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}