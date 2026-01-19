package org.ject.recreation.core.domain.admin;

import org.ject.recreation.core.api.controller.request.GameDeleteRequestDto;
import org.ject.recreation.core.api.controller.response.ReportGameDetailResponseDto;
import org.ject.recreation.core.api.controller.response.ReportGameResponseDto;
import org.ject.recreation.storage.db.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdminServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdminService adminService;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private GameRepository gameRepository;

    private final List<GameEntity> games = new ArrayList<>();
    private ReportEntity testReport1;

    @BeforeEach
    void setUp() {
        UserEntity creator = UserEntity.builder()
                .email("test@example.com")
                .platform("kakao")
                .profileImageUrl("https://example.com/profile.png")
                .nickname("Test")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(creator);

        for (int i = 0; i < 10; i++) {
            UUID gameId = UUID.randomUUID();

            GameEntity game = GameEntity.builder()
                    .gameId(gameId)
                    .gameCreator(creator)
                    .gameTitle("Game " + i)
                    .gameThumbnailUrl("https://example.com/thumbnail.png")
                    .build();

            QuestionEntity question = QuestionEntity.builder()
                    .questionOrder(1)
                    .questionText("Question 1")
                    .questionAnswer("Answer 1")
                    .game(game)
                    .build();
            game.addQuestion(question);
            game.setQuestionCount(1);

            games.add(gameRepository.save(game));
        }

        testReport1 = reportRepository.save(ReportEntity.toEntity(games.get(0), null, "SPAM_OR_PROMOTION"));
        reportRepository.save(ReportEntity.toEntity(games.get(1), null, "SEXUAL_CONTENT"));
        reportRepository.save(ReportEntity.toEntity(games.get(2), null, "VIOLENT_OR_DISTURBING_CONTENT"));
    }

    @Test
    void getReportedGamesTest() {
        // when
        Page<ReportGameResponseDto> reportedGames = adminService.getReportedGames(0);

        System.out.print("=========================");
        System.out.println("Reported Games Details:");
        reportedGames.forEach(dto -> {
            System.out.println("  Report ID: " + dto.getReportId());
            System.out.println("  Game ID: " + dto.getGameId());
            System.out.println("  Game Title: " + dto.getGameName());
            System.out.println("  Creator Nickname: " + dto.getCreatorName());
            System.out.println("  Reporter Nickname: " + dto.getReporterName());
            System.out.println("  Reported At: " + dto.getReportedAt());
            System.out.println("  Status: " + dto.getStatus());
            System.out.println("-------------------------");
        });
        System.out.print("=========================");
        // then
        assertNotNull(reportedGames);
        assertEquals(3, reportedGames.getTotalElements());
        assertEquals(3, reportedGames.getContent().size());
    }
    @Test
    void getReportedDetailGamesTest() {
        // given
        long reportId = testReport1.getId();

        // when
        ReportGameDetailResponseDto reportDetail = adminService.getReportedDetailGames(reportId);

        // then
        assertNotNull(reportDetail);
        // Add more specific assertions if needed, e.g., assertEquals(testReport1.getGame().getGameTitle(), reportDetail.getGameTitle());

        System.out.print("=========================");
        System.out.println("Report Detail for ID " + reportId + ":");
        System.out.println("  Game Title: " + reportDetail.getGameTitle());
        System.out.println("  Maker Nickname: " + reportDetail.getMakerNickname());
        System.out.println("  Nickname (maybe reporter?): " + reportDetail.getMakerEmail());
        System.out.println("  Question Count: " + reportDetail.getQuestionCount());
        System.out.println("  Version: " + reportDetail.getVersion());
        System.out.println("  Reporter Email: " + reportDetail.getReporterEmail());
        System.out.println("  Reporter Nickname: " + reportDetail.getReporterNickname());
        System.out.println("  Reason Code: " + reportDetail.getReasonCode());

        // Print questions if available
        if (reportDetail.getQustions() != null && !reportDetail.getQustions().isEmpty()) {
            System.out.println("  Questions:");
            reportDetail.getQustions().forEach(q -> {
                System.out.println("    - Question ID: " + q.questionId());
                System.out.println("      Order: " + q.questionOrder());
                System.out.println("      Text: " + q.questionText());
                System.out.println("      Answer: " + q.questionAnswer());
                System.out.println("      Version: " + q.version());
            });
        }
        System.out.println("-------------------------");
        System.out.print("=========================");
    }

    @Test
    void deleteReportedGameTest() {
        // given
        long initialReportCount = reportRepository.count();
        long reportIdToDelete = testReport1.getId();
        GameDeleteRequestDto requestDto = GameDeleteRequestDto.builder()
                .reportId(reportIdToDelete)
                .reportStatus(ReportStatus.GAME_DELETED)
                .build();

        // when
        adminService.deleteReportedDetailGames(requestDto);

        // then
        long finalReportCount = reportRepository.count();
        assertEquals(initialReportCount - 1, finalReportCount);
        assertFalse(reportRepository.findById(reportIdToDelete).isPresent());
    }
}