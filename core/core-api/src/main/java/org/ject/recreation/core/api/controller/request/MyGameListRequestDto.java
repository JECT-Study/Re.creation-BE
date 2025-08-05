package org.ject.recreation.core.api.controller.request;

import jakarta.validation.constraints.Min;
import org.ject.recreation.core.api.controller.validation.AllOrNone;
import org.ject.recreation.core.api.controller.validation.contract.HasCursorFields;
import org.ject.recreation.core.domain.user.MyGameListQuery;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@AllOrNone
public record MyGameListRequestDto(
        UUID cursorGameId,
        LocalDateTime cursorUpdatedAt,
        @Min(1)
        int limit
)  implements HasCursorFields {

    @Override
    public List<Object> getCursorFields() {
        // List.of는 NULL 허용 X
        return Arrays.asList(cursorUpdatedAt, cursorGameId);
    }

    public MyGameListQuery toMyGameListQuery() {
        return new MyGameListQuery(
                cursorGameId,
                cursorUpdatedAt,
                limit
        );
    }
}
