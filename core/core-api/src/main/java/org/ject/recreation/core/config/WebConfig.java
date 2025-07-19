package org.ject.recreation.core.config;

import lombok.RequiredArgsConstructor;
import org.ject.recreation.core.api.controller.session.SessionUserEmailArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final SessionUserEmailArgumentResolver sessionUserEmailArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(sessionUserEmailArgumentResolver);
    }
} 