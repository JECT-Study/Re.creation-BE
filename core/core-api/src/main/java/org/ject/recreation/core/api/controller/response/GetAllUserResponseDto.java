package org.ject.recreation.core.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.recreation.storage.db.core.ReportEntity;
import org.ject.recreation.storage.db.core.UserEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllUserResponseDto {
    List<UserInfoDto> contents;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfoDto {
        private String nickname;
        private String email;
        private String blockReason;
        private LocalDate blockedAt;
        private boolean blocked;

        public static UserInfoDto from(UserEntity user) {
            return UserInfoDto.builder()
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .blockReason(user.getBlockReason() != null ? user.getBlockReason().name() : null) //
                    .blockedAt(user.getBlockedAt())
                    .blocked(user.getBlockedAt() != null)
                    .build();
        }
    }

    public static GetAllUserResponseDto from(List<UserEntity> users) {
        List<UserInfoDto> userInfoDtos = users.stream()
                .map(UserInfoDto::from)
                .collect(Collectors.toList());
        return GetAllUserResponseDto.builder()
                .contents(userInfoDtos)
                .build();
    }
}
