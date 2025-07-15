package com.lqviet.baseentity.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import jakarta.persistence.Entity;

/**
 * Auto-configuration for Base Entity functionality.
 *
 * @author Le Quoc Viet
 * @version 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass({Entity.class})
@EnableJpaRepositories
@Import(AuditingConfig.class)
public class BaseEntityAutoConfiguration {
    // Auto-configuration is handled by imported classes
}
