package org.ject.recreation.core.api.controller.request;

import org.ject.recreation.core.domain.SampleData;

public record SampleRequestDto(String data) {
    public SampleData toSampleData() {
        return new SampleData(data, data);
    }
}
