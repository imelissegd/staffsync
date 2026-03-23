package com.mgd.employee_mgmt.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;

/**
 * Application-level Spring configuration.
 *
 * Registers:
 *   - MessageSource  → resolves keys from messages.properties
 *   - urls.properties is loaded via @PropertySource on each controller/service
 *     that needs URL values, or globally via application.properties import.
 */
@Configuration
public class AppConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source =
                new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        source.setCacheSeconds(-1); // cache forever in production
        return source;
    }
}
