package org.ject.recreation.core.api.controller;

import org.ject.recreation.core.api.controller.request.SampleRequestDto;
import org.ject.recreation.core.api.controller.response.SampleResponseDto;
import org.ject.recreation.core.domain.SampleResult;
import org.ject.recreation.core.domain.SampleService;
import org.ject.recreation.core.support.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @GetMapping("/sample")
    public ApiResponse<SampleResponseDto> example(@RequestBody SampleRequestDto request) {
        SampleResult sampleResult = sampleService.sampleBusinessLogic(request.toSampleData());
        return ApiResponse.success(new SampleResponseDto(sampleResult.data()));
    }
}
