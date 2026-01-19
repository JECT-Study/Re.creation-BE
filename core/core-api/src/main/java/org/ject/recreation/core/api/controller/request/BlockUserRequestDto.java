package org.ject.recreation.core.api.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.recreation.storage.db.core.ReportReason;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockUserRequestDto {

    @Builder.Default
    private List<UserBanItem> banList = new ArrayList<>();

    @Getter
    public static class UserBanItem{
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        private String email;
        private ReportReason reason;
    }
}
