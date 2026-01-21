package org.ject.recreation.core.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.recreation.core.domain.game.GameReportReason;
import org.ject.recreation.storage.db.core.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGameDetailResponseDto {
    private String gameTitle;
    private String makerNickname;
    private String makerEmail;
    private boolean isMakerBlock;
    private ReportStatus status;
    private int questionCount;
    private long version;
    private List<GameDetailResponseDto.QuestionDto> qustions;
    private String reporterEmail;
    private String reporterNickname;
    private boolean isReporterBlock;
    private GameReportReason reasonCode;

    public static ReportGameDetailResponseDto from(ReportEntity reportEntity) {
        GameEntity game = reportEntity.getGame();
        UserEntity gameCreator = game.getGameCreator();
        UserEntity reporter = reportEntity.getReporter();
        List<QuestionEntity> questions = game.getQuestions();

        ReportReason reason = reportEntity.getReason();
        GameReportReason gameReportReason = GameReportReason.valueOf(reason.name());

        List<GameDetailResponseDto.QuestionDto> list = questions.stream()
                .map(question -> new GameDetailResponseDto.QuestionDto(
                        question.getQuestionId(),
                        question.getQuestionOrder(),
                        question.getImageUrl(),
                        question.getQuestionText(),
                        question.getQuestionAnswer(),
                        question.getVersion()))
                .toList();

        return ReportGameDetailResponseDto.builder()
                .gameTitle(game.getGameTitle())
                .makerNickname(gameCreator.getNickname())
                .makerEmail(gameCreator.getEmail())
                .isMakerBlock(gameCreator.getBlockReason() != null)
                .status(reportEntity.getStatus())
                .questionCount(game.getQuestionCount())
                .version(game.getVersion())
                .qustions(list)
                .reporterEmail(reporter != null ? reporter.getEmail() : null)
                .reporterNickname(reporter != null ? reporter.getNickname() : null)
                .isReporterBlock(reporter.getBlockReason() != null)
                .reasonCode(gameReportReason)
                .build();
    }
}
