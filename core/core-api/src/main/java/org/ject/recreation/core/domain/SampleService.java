package org.ject.recreation.core.domain;

import org.springframework.stereotype.Service;

@Service
public class SampleService {
    public SampleResult sampleBusinessLogic(SampleData sampleData) {
        return new SampleResult(sampleData.value());
    }
}
