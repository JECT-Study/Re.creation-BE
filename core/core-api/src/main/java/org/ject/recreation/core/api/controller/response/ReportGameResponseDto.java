package org.ject.recreation.core.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.recreation.storage.db.core.GameEntity;
import org.ject.recreation.storage.db.core.ReportEntity;
import org.ject.recreation.storage.db.core.ReportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGameResponseDto {
    private Long reportId;
    private UUID gameId;
    private String gameName;
    private String creatorName;
    private String reporterName;
    private LocalDateTime reportedAt;
    private ReportStatus status;

    public static ReportGameResponseDto from(ReportEntity report) {
        return ReportGameResponseDto.builder()
                .reportId(report.getId())
                .gameId(report.getGame().getGameId())
                .gameName(report.getGame().getGameTitle())
                .creatorName(report.getGame().getGameCreator().getNickname())
                .reporterName(
                        report.getReporter() != null
                                ? report.getReporter().getNickname()
                                : null
                )
                .reportedAt(report.getReportedAt())
                .status(report.getStatus())
                .build();
    }
}
