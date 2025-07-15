package com.lqviet.baseentity.annotations;

import com.lqviet.baseentity.config.BaseEntityAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Annotation to enable Base Entity functionality.
 *
 * @author Le Quoc Viet
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(BaseEntityAutoConfiguration.class)
public @interface EnableBaseEntity {
}

