package com.lqviet.baseentity.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Configuration class for JPA auditing functionality.
 * Supports both Spring Security and non-Spring Security environments.
 *
 * @author Le Quoc Viet
 * @version 1.0.0
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    // Default auditor when Spring Security is NOT present
    @Bean
    @ConditionalOnMissingBean
    public AuditorAware<String> auditorProvider() {
        return new DefaultAuditorAware(); // Returns "system"
    }

    // Spring Security auditor when Spring Security IS present
    @Configuration
    @ConditionalOnClass(name = "org.springframework.security.core.Authentication")
    static class SpringSecurityAuditingConfig {

        @Bean
        public AuditorAware<String> springSecurityAuditorProvider() {
            return new SpringSecurityAuditorAware(); // Uses security context
        }
    }

    /**
     * Default auditor provider when Spring Security is not available.
     */
    public static class DefaultAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            // Try to get user from thread local or other context
            String currentUser = getCurrentUserFromContext();
            return Optional.ofNullable(currentUser).or(() -> Optional.of("system"));
        }

        private String getCurrentUserFromContext() {
            // Check for common patterns like ThreadLocal user context
            try {
                // Example: Check if there's a custom user context
                // String user = UserContext.getCurrentUser();
                // return user;

                // For now, return system - users can override this bean
                return "system";
            } catch (Exception e) {
                return "system";
            }
        }
    }

    /**
     * Spring Security-aware auditor provider.
     * Uses Spring Security context to determine current user.
     */
    public static class SpringSecurityAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            try {
                // Use reflection to avoid compile-time dependency on Spring Security
                Class<?> securityContextHolderClass = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
                Object securityContext = securityContextHolderClass.getMethod("getContext").invoke(null);
                Object authentication = securityContext.getClass().getMethod("getAuthentication").invoke(securityContext);

                if (authentication == null) {
                    return Optional.of("system");
                }

                Boolean isAuthenticated = (Boolean) authentication.getClass().getMethod("isAuthenticated").invoke(authentication);
                if (!isAuthenticated) {
                    return Optional.of("system");
                }

                Object principal = authentication.getClass().getMethod("getPrincipal").invoke(authentication);

                if ("anonymousUser".equals(principal)) {
                    return Optional.of("system");
                }

                // Try UserDetails first
                try {
                    String username = (String) principal.getClass().getMethod("getUsername").invoke(principal);
                    return Optional.of(username);
                } catch (Exception e) {
                    // Fall back to string principal
                    if (principal instanceof String) {
                        return Optional.of((String) principal);
                    }
                }

                // Fall back to authentication name
                String name = (String) authentication.getClass().getMethod("getName").invoke(authentication);
                return Optional.ofNullable(name).or(() -> Optional.of("system"));

            } catch (Exception e) {
                // If anything goes wrong, fall back to system
                return Optional.of("system");
            }
        }
    }
}
