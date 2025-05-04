package com.jinn.ratejinn.config.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RedisConfigValidator.class)
public @interface ValidRedisConfig {
    String message() default "Invalid Redis configuration";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
