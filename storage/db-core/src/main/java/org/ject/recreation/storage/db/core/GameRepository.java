package org.ject.recreation.storage.db.core;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GameRepository extends JpaRepository<GameEntity, UUID> {

    @Query("""
        SELECT g FROM GameEntity g
        WHERE g.isDeleted = false
        AND g.isShared = true
        AND (:query IS NULL OR g.gameTitle LIKE %:query%)
        AND (:cursorPlayCount IS NULL
            OR g.playCount < :cursorPlayCount
            OR (g.playCount = :cursorPlayCount AND g.updatedAt < :cursorUpdatedAt)
            OR (g.playCount = :cursorPlayCount AND g.updatedAt = :cursorUpdatedAt AND g.gameId < :cursorGameId)
        )
        ORDER BY g.playCount DESC, g.updatedAt DESC, g.gameId DESC
    """)
    List<GameEntity> findGamesWithCursorAndQuery(
            @Param("cursorGameId") UUID cursorGameId,
            @Param("cursorPlayCount") Long cursorPlayCount,
            @Param("cursorUpdatedAt") LocalDateTime cursorUpdatedAt,
            Pageable pageable,
            @Param("query") String query
    );

    @Query("""
        SELECT g FROM GameEntity g
        WHERE g.isDeleted = false
        AND g.gameCreator.email = :gameCreatorEmail
        AND (:cursorUpdatedAt IS NULL
            OR g.updatedAt < :cursorUpdatedAt
            OR (g.updatedAt = :cursorUpdatedAt AND g.gameId < :cursorGameId)
        )
        ORDER BY g.updatedAt DESC, g.gameId DESC
    """)
    List<GameEntity> findGamesWithEmailAndCursor(
            @Param("cursorGameId") UUID cursorGameId,
            @Param("cursorUpdatedAt") LocalDateTime cursorUpdatedAt,
            @Param("gameCreatorEmail") String gameCreatorEmail,
            Pageable pageable
    );

    // 기본 게임들을 조회하는 메서드
    List<GameEntity> findAllByGameCreatorEmailAndIsDeletedFalse(String gameCreatorEmail);
}
