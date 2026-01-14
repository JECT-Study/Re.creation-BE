package org.ject.recreation.core.api.controller.request;

import lombok.*;
import org.ject.recreation.storage.db.core.ReportStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameDeleteRequestDto {
    private ReportStatus reportStatus;
    private Long reportId;
}
