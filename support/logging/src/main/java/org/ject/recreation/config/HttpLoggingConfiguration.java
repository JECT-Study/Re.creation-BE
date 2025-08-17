package org.ject.recreation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ject.recreation.HttpRequestLoggingFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;

@Configuration
@ConditionalOnClass(ObjectMapper.class)
public class HttpLoggingConfiguration {

    @Bean
    @Profile({"local", "prod"})
    @ConditionalOnMissingBean(HttpRequestLoggingFilter.class)
    public FilterRegistrationBean<HttpRequestLoggingFilter> loggingFilter(ObjectMapper objectMapper) {
        FilterRegistrationBean<HttpRequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new HttpRequestLoggingFilter(objectMapper));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.setName("httpRequestLoggingFilter");

        return registrationBean;
    }

}
