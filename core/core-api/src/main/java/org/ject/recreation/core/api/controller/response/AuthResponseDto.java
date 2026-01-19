package org.ject.recreation.core.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
import org.ject.recreation.storage.db.core.UserEntity;
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

    public static AuthResponseDto createResponseDto(UserEntity userEntity) {
        return AuthResponseDto.builder()
                .email(userEntity.getEmail())
                .nickname(userEntity.getNickname())
                .role(userEntity.getRole())
                .blocked(userEntity.getRole() == UserRole.BLOCK)
                .build();
    }
}
