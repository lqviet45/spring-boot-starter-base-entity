package com.lqviet.baseentity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configuration class for JPA auditing functionality.
 *
 * @author Le Quoc Viet
 * @version 1.0.0
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

    public static class SpringSecurityAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication == null || !authentication.isAuthenticated() ||
                        "anonymousUser".equals(authentication.getPrincipal())) {
                    return Optional.of("system");
                }

                if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                    org.springframework.security.core.userdetails.UserDetails userDetails =
                            (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
                    return Optional.of(userDetails.getUsername());
                }

                if (authentication.getPrincipal() instanceof String) {
                    return Optional.of((String) authentication.getPrincipal());
                }

                return Optional.ofNullable(authentication.getName()).or(() -> Optional.of("system"));
            } catch (Exception e) {
                return Optional.of("system");
            }
        }
    }
}
