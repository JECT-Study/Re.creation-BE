package org.ject.recreation.storage.db.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "report")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게임에 대한 신고인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    // 신고자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", nullable = true)
    private UserEntity reporter;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    @Enumerated(EnumType.STRING)
    private ReportStatus status; // 미처리 / 게임삭제 / 신고무시

    private LocalDateTime reportedAt;

    public static ReportEntity toEntity(GameEntity game, UserEntity reporter, String reason) {
        ReportReason reportReason = ReportReason.getReportReason(reason);
        return ReportEntity.builder()
                .game(game)
                .reporter(reporter)
                .reason(reportReason)
                .status(ReportStatus.PENDING)
                .reportedAt(LocalDateTime.now())
                .build();
    }
}
