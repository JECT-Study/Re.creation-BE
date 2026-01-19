package org.ject.recreation.core.api.controller.request;

import lombok.*;
import org.ject.recreation.core.domain.game.GameReportReason;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportGameRequestDto {
    private GameReportReason reasonCode;
}
