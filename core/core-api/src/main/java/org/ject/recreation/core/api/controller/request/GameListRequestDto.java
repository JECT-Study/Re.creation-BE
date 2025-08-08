package org.ject.recreation.core.api.controller.request;

import jakarta.validation.constraints.Min;
import org.ject.recreation.core.api.controller.validation.AllOrNone;
import org.ject.recreation.core.api.controller.validation.contract.HasCursorFields;
import org.ject.recreation.core.domain.game.GameListQuery;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllOrNone
public record GameListRequestDto(
        UUID cursorGameId,
        Long cursorPlayCount,
        LocalDateTime cursorUpdatedAt,
        @Min(1)
        int limit,
        String query
) implements HasCursorFields {

    @Override
    public List<Object> getCursorFields() {
        // List.of는 NULL 허용 X
        return Arrays.asList(cursorPlayCount, cursorUpdatedAt, cursorGameId);
    }

    public GameListQuery toGameListQuery() {
        return new GameListQuery(
                cursorGameId,
                cursorPlayCount,
                cursorUpdatedAt,
                limit,
                query
        );
    }
}
