package org.ject.recreation.storage.db.core;

public enum ReportStatus {
    PENDING,        // 미처리
    GAME_DELETED,   // 게임 삭제
    IGNORED;         // 신고 무시

    public static boolean isDelete(ReportStatus status) {
        return status == GAME_DELETED;
    }
}
