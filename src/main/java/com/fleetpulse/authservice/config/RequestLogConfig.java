package com.fleetpulse.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Abilita il logging delle richieste HTTP in ingresso all'auth-service.
 * <p>
 * Ogni richiesta viene loggata con method, URI, header e body.
 * Il livello di logging e' configurato in log4j2-spring.xml per il logger
 * {@code org.springframework.web.filter.CommonsRequestLoggingFilter}.
 * </p>
 */
@Configuration
public class RequestLogConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setIncludeHeaders(true);
        filter.setMaxPayloadLength(10000);
        filter.setAfterMessagePrefix(">>> ");
        return filter;
    }

}
