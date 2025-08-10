package org.ject.recreation.storage.db.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "org.ject.recreation.storage.db.core")
@EnableJpaRepositories(basePackages = "org.ject.recreation.storage.db.core")
class CoreJpaConfig {

}
