package org.ject.recreation.core.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
import org.ject.recreation.storage.db.core.UserRole;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {
    private String email;
    private String nickname;
    @Builder.Default
    private UserRole role = UserRole.USER;
    @Builder.Default
    boolean blocked = false;

    public static AuthResponseDto createResponseDto(SessionUserInfoDto sessionUserInfoDto) {
        return AuthResponseDto.builder()
                .email(sessionUserInfoDto.getEmail())
                .nickname(sessionUserInfoDto.getNickname())
                .build();
    }
}
